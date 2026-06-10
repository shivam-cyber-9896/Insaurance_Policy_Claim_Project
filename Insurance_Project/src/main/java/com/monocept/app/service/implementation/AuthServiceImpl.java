package com.monocept.app.service.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.LoginRequestDto;
import com.monocept.app.dto.LoginResponseDto;
import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.User;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.security.JwtService;
import com.monocept.app.service.AuthService;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final ModelMapper modelMapper;
	private final EmailService emailService;
	private final EmailTempleteService emailTemplateService;

	@Override
	public UserResponseDto register(UserRequestDto dto) {

		log.info("Registering user");

		if (dto.getRole() != null && dto.getRole() != com.monocept.app.enums.Role.CUSTOMER) {
			throw new com.monocept.app.exception.InvalidOperationException(
					"Public registration is allowed only for customers");
		}

		if (userRepository.existsByEmail(dto.getEmail())) {

			throw new DuplicateResourceException("Email already exists");
		}

		User user = modelMapper.map(dto, User.class);

		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(com.monocept.app.enums.Role.CUSTOMER);
		user.setActive(true);

		User savedUser = userRepository.save(user);
		// send welcome email
		emailService.sendEmail(savedUser.getEmail(), "Welcome to Insurance Portal",
				emailTemplateService.welcomeTemplate(savedUser.getFullName(), savedUser.getEmail()));

		log.info("User registered successfully");

		return modelMapper.map(savedUser, UserResponseDto.class);
	}

	@Override
	public LoginResponseDto login(LoginRequestDto dto) {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

		User user = userRepository.findByEmail(dto.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		String token = jwtService.generateToken(user.getEmail());
		// send login alert email
		String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

		emailService.sendEmail(user.getEmail(), "New Login Detected",
				emailTemplateService.loginAlertTemplate(user.getFullName(), user.getEmail(), loginTime));

		return LoginResponseDto.builder().token(token).email(user.getEmail()).fullName(user.getFullName())
				.role(user.getRole()).build();
	}
}