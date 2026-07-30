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
import com.monocept.app.enums.OtpPurpose;
import com.monocept.app.exception.CustomExceptions.DuplicateResourceException;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.exception.ResourceNotFoundException;
import com.monocept.app.model.OtpVerification;
import com.monocept.app.model.User;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.security.JwtService;
import com.monocept.app.service.AuthService;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.EmailTempleteService;
import com.monocept.app.service.OtpService;
import com.monocept.app.repository.OtpVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

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
	private final OtpVerificationRepository otpRepository;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

		boolean isFullyVerified = otpService.verifyEmailOtp(dto.getEmail(), dto.getOtp());

		if (isFullyVerified) {
			user.setActive(true);
			userRepository.save(user);

			emailService.sendEmail(user.getEmail(), "Welcome to Insurance Portal",
					emailTemplateService.welcomeTemplate(user.getFullName(), user.getEmail()));

			log.info("User account activated successfully");
		} else {
			log.info("Email OTP verified successfully. Waiting for mobile OTP verification.");
		}
	}

	@Override
	public void verifyMobileRegistration(OtpRequestDto dto) {
		log.info("Verifying mobile registration OTP for email: {}", dto.getEmail());

		User user = userRepository.findByEmail(dto.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + dto.getEmail()));

		if (user.isActive()) {
			throw new com.monocept.app.exception.InvalidOperationException("User is already active");
		}

		boolean isFullyVerified = otpService.verifyMobileOtp(dto.getEmail(), dto.getOtp());

		if (isFullyVerified) {
			user.setActive(true);
			userRepository.save(user);

			emailService.sendEmail(user.getEmail(), "Welcome to Insurance Portal",
					emailTemplateService.welcomeTemplate(user.getFullName(), user.getEmail()));

			log.info("User account activated successfully");
		} else {
			log.info("Mobile OTP verified successfully. Waiting for email OTP verification.");
		}
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

		String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

		String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

		try {
			emailService.sendEmail(user.getEmail(), "New Login Detected",
					emailTemplateService.loginAlertTemplate(user.getFullName(), user.getEmail(), loginTime));
		} catch (Exception e) {
			// Don't let a flaky mail server block a successful login
			log.warn("Failed to send login alert email for {}: {}", user.getEmail(), e.getMessage());
		}

		return LoginResponseDto.builder().token(token).email(user.getEmail()).fullName(user.getFullName())
				.role(user.getRole()).build();
	}

	@Override
	public void forgotPassword(String email) {
		log.info("Requesting password reset for email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		if (!user.isActive()) {
			throw new InvalidOperationException("User account is inactive. Cannot reset password.");
		}

		String resetOtp = String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

		OtpVerification verification = otpRepository
				.findByEmailAndPurpose(email, OtpPurpose.PASSWORD_RESET)
				.orElse(new OtpVerification());

		verification.setEmail(email);
		verification.setOtp(resetOtp);
		verification.setExpiresAt(expiresAt);
		verification.setPurpose(OtpPurpose.PASSWORD_RESET);
		verification.setMobileOtp(null);
		verification.setMobileExpiresAt(expiresAt);
		verification.setEmailVerified(false);
		verification.setMobileVerified(false);

		otpRepository.save(verification);

		String subject = "Password Recovery OTP";
		String htmlBody = "<h3>Password Reset Request</h3>"
				+ "<p>Please use the following One-Time Password (OTP) to reset your account password:</p>"
				+ "<h2 style='color:#1a73e8; letter-spacing: 2px;'>" + resetOtp + "</h2>"
				+ "<p>This OTP is valid for <b>5 minutes</b>. Please do not share this code with anyone.</p>";

		emailService.sendEmail(email, subject, htmlBody);
	}

	@Override
	@org.springframework.transaction.annotation.Transactional
	public void resetPassword(String email, String otp, String newPassword) {
		log.info("Resetting password for email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		if (!user.isActive()) {
			throw new InvalidOperationException("User account is inactive. Cannot reset password.");
		}

		OtpVerification verification = otpRepository
				.findByEmailAndPurpose(email, OtpPurpose.PASSWORD_RESET)
				.orElseThrow(() -> new InvalidOperationException(
						"No OTP requested or OTP has expired for this email"));

		if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
			otpRepository.delete(verification);
			throw new InvalidOperationException("OTP has expired. Please request a new one.");
		}

		boolean matches = MessageDigest.isEqual(
				verification.getOtp().getBytes(StandardCharsets.UTF_8),
				otp.getBytes(StandardCharsets.UTF_8));

		if (!matches) {
			throw new InvalidOperationException("Invalid recovery code. Please try again.");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		otpRepository.delete(verification);
		log.info("Password reset successful. Deleted OTP verification record.");
	}
}