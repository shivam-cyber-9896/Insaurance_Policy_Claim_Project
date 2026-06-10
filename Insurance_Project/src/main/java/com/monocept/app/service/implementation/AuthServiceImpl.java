package com.monocept.app.service.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.monocept.app.dto.LoginRequestDto;
import com.monocept.app.dto.LoginResponseDto;
import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.OtpRequestDto;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.User;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.security.JwtService;
import com.monocept.app.service.AuthService;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;
import com.monocept.app.service.OtpService;

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
	private final OtpService otpService;

	@Override
	public UserResponseDto register(UserRequestDto dto) {

		log.info("Registering user: {}", dto.getEmail());

		if (dto.getRole() != null && dto.getRole() != com.monocept.app.enums.Role.CUSTOMER) {
			throw new com.monocept.app.exception.InvalidOperationException(
					"Public registration is allowed only for customers");
		}

		Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());
		if (existingUserOpt.isPresent()) {
			User existingUser = existingUserOpt.get();
			if (existingUser.isActive()) {
				throw new DuplicateResourceException("Email already exists");
			} else {
				log.info("User already exists but is inactive. Updating details and resending OTP.");
				existingUser.setFullName(dto.getFullName());
				existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
				existingUser.setPhoneNumber(dto.getPhoneNumber());
				existingUser.setRole(com.monocept.app.enums.Role.CUSTOMER);
				User savedUser = userRepository.save(existingUser);

				otpService.sendOtp(savedUser.getEmail());

				log.info("Inactive user registration details updated and OTP sent.");
				return modelMapper.map(savedUser, UserResponseDto.class);
			}
		}

		User user = modelMapper.map(dto, User.class);

		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(com.monocept.app.enums.Role.CUSTOMER);
		user.setActive(false); // Make inactive initially

		User savedUser = userRepository.save(user);
		
		otpService.sendOtp(savedUser.getEmail());

		log.info("User registered successfully (inactive). OTP sent.");

		return modelMapper.map(savedUser, UserResponseDto.class);
	}

	@Override
	public void verifyRegistration(OtpRequestDto dto) {
		log.info("Verifying registration OTP for email: {}", dto.getEmail());

		User user = userRepository.findByEmail(dto.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + dto.getEmail()));

		if (user.isActive()) {
			throw new com.monocept.app.exception.InvalidOperationException("User is already active");
		}

		otpService.verifyOtp(dto.getEmail(), dto.getOtp());

		user.setActive(true);
		userRepository.save(user);

		// Send welcome email
		emailService.sendEmail(user.getEmail(), "Welcome to Insurance Portal",
				emailTemplateService.welcomeTemplate(user.getFullName(), user.getEmail()));

		log.info("User account activated successfully");
	}

	@Override
	public void resendOtp(String email) {
		log.info("Resending OTP for email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		if (user.isActive()) {
			throw new com.monocept.app.exception.InvalidOperationException("User is already active");
		}

		otpService.sendOtp(email);
	}

	@Override
	public LoginResponseDto login(LoginRequestDto dto) {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

		User user = userRepository.findByEmail(dto.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (!user.isActive()) {
			throw new com.monocept.app.exception.InvalidOperationException("User is inactive. Please verify your OTP first.");
		}

		String token = jwtService.generateToken(user.getEmail());
		// send login alert email
		String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

		emailService.sendEmail(user.getEmail(), "New Login Detected",
				emailTemplateService.loginAlertTemplate(user.getFullName(), user.getEmail(), loginTime));

		return LoginResponseDto.builder().token(token).email(user.getEmail()).fullName(user.getFullName())
				.role(user.getRole()).build();
	}
}