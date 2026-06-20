package com.monocept.app.service.implementation;

import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.model.OtpVerification;
import com.monocept.app.repository.OtpVerificationRepository;
import com.monocept.app.repository.UserRepository;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.OtpService;
import com.monocept.app.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public void sendOtp(String email) {
        log.info("Generating OTPs for email: {}", email);

        // Generate 6 digit numeric OTPs
        String emailOtp = String.valueOf(100000 + random.nextInt(900000));
        String mobileOtp = String.valueOf(100000 + random.nextInt(900000));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Save or update OTP verification record
        OtpVerification verification = otpRepository.findByEmail(email)
                .orElse(new OtpVerification());
        
        verification.setEmail(email);
        verification.setOtp(emailOtp);
        verification.setExpiresAt(expiresAt);
        verification.setMobileOtp(mobileOtp);
        verification.setMobileExpiresAt(expiresAt);
        verification.setEmailVerified(false);
        verification.setMobileVerified(false);

        otpRepository.save(verification);

        // Send OTP via email
        String subject = "Email Verification OTP";
        String htmlBody = "<h3>Email Verification</h3>"
                + "<p>Thank you for registering. Please use the following One-Time Password (OTP) to complete your registration:</p>"
                + "<h2 style='color:#1a73e8; letter-spacing: 2px;'>" + emailOtp + "</h2>"
                + "<p>This OTP is valid for <b>5 minutes</b>. Please do not share this code with anyone.</p>";

        emailService.sendEmail(email, subject, htmlBody);

        // Send OTP via SMS
        userRepository.findByEmail(email).ifPresent(user -> {
            String phoneNumber = user.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                String smsBody = "Thank you for registering. Your mobile verification OTP is: " + mobileOtp + ". Valid for 5 minutes.";
                smsService.sendSms(phoneNumber, smsBody);
            }
        });
    }

    @Override
    @Transactional
    public boolean verifyEmailOtp(String email, String code) {
        log.info("Verifying email OTP for email: {}", email);

        OtpVerification verification = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No OTP requested or OTP has expired for this email"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(verification);
            throw new InvalidOperationException("OTP has expired. Please request a new one.");
        }

        if (!verification.getOtp().equals(code)) {
            throw new InvalidOperationException("Invalid email OTP code. Please try again.");
        }

        verification.setEmailVerified(true);

        if (verification.isMobileVerified()) {
            otpRepository.delete(verification);
            log.info("Both email and mobile OTPs verified successfully. Deleting verification record.");
            return true;
        } else {
            otpRepository.save(verification);
            log.info("Email OTP verified successfully. Waiting for mobile OTP verification.");
            return false;
        }
    }

    @Override
    @Transactional
    public boolean verifyMobileOtp(String email, String code) {
        log.info("Verifying mobile OTP for email: {}", email);

        OtpVerification verification = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No OTP requested or OTP has expired for this email"));

        if (verification.getMobileExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(verification);
            throw new InvalidOperationException("OTP has expired. Please request a new one.");
        }

        if (!verification.getMobileOtp().equals(code)) {
            throw new InvalidOperationException("Invalid mobile OTP code. Please try again.");
        }

        verification.setMobileVerified(true);

        if (verification.isEmailVerified()) {
            otpRepository.delete(verification);
            log.info("Both email and mobile OTPs verified successfully. Deleting verification record.");
            return true;
        } else {
            otpRepository.save(verification);
            log.info("Mobile OTP verified successfully. Waiting for email OTP verification.");
            return false;
        }
    }
}
