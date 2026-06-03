package com.example.app.service;

import com.example.app.dto.LoginRequestDto;
import com.example.app.dto.LoginResponseDto;
import com.example.app.dto.RegisterRequestDto;
import com.example.app.enums.Role;
import com.example.app.exception.CustomExceptions;
import com.example.app.model.Customer;
import com.example.app.model.User;
import com.example.app.repository.CustomerRepository;
import com.example.app.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public String registerCustomer(RegisterRequestDto request) {
        if (userRepository.existsByMail(request.getEmail())) {
            throw new CustomExceptions.DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        // Map DTO to User entity
        User user = modelMapper.map(request, User.class);
        user.setMail(request.getEmail()); // Explicitly set mail to prevent validation errors due to name mismatch
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        userRepository.save(user);

        return "Customer registered successfully";
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByMail(request.getEmail())
                .orElseThrow(() -> new CustomExceptions.InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new CustomExceptions.InactiveUserException("Account is inactive. Please contact admin.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomExceptions.InvalidCredentialsException("Invalid email or password");
        }

        // Map User entity to LoginResponseDto
        LoginResponseDto response = modelMapper.map(user, LoginResponseDto.class);
        response.setEmail(user.getMail()); // Explicitly map mail to email due to field name mismatch
        response.setStatusMessage("Login Successful");
        return response;
    }
}
