package com.monocept.app.service.implementation;

import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.monocept.app.exception.InvalidOperationException;
import com.monocept.app.model.OtpVerification;
import com.monocept.app.repository.OtpVerificationRepository;
import com.monocept.app.service.EmailService;
import com.monocept.app.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    @Override
    @Transactional
    public void sendOtp(String email) {
        log.info("Generating OTP for email: {}", email);

        // Generate 6 digit numeric OTP
        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Save or update OTP verification record
        OtpVerification verification = otpRepository.findByEmail(email)
                .orElse(new OtpVerification());
        
        verification.setEmail(email);
        verification.setOtp(otp);
        verification.setExpiresAt(expiresAt);

        otpRepository.save(verification);

        // Send OTP via email
        String subject = "Email Verification OTP";
        String htmlBody = "<h3>Email Verification</h3>"
                + "<p>Thank you for registering. Please use the following One-Time Password (OTP) to complete your registration:</p>"
                + "<h2 style='color:#1a73e8; letter-spacing: 2px;'>" + otp + "</h2>"
                + "<p>This OTP is valid for <b>5 minutes</b>. Please do not share this code with anyone.</p>";

        emailService.sendEmail(email, subject, htmlBody);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String code) {
        log.info("Verifying OTP for email: {}", email);

        OtpVerification verification = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No OTP requested or OTP has expired for this email"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(verification);
            throw new InvalidOperationException("OTP has expired. Please request a new one.");
        }

        if (!verification.getOtp().equals(code)) {
            throw new InvalidOperationException("Invalid OTP code. Please try again.");
        }

        // Successfully verified, delete the OTP to prevent replay attacks
        otpRepository.delete(verification);
        log.info("OTP verified successfully for email: {}", email);
    }
}
