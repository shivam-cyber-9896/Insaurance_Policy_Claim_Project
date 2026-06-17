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
    private final Random random = new Random();

    @Override
    @Transactional
    public void sendOtp(String email, String phoneNumber) {

        log.info("Generating OTP for email: {}", email);

        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpVerification verification = otpRepository.findByEmail(email)
                .orElse(new OtpVerification());

        verification.setEmail(email);
        verification.setPhoneNumber(phoneNumber);
        verification.setOtp(otp);
        verification.setExpiresAt(expiresAt);
        otpRepository.save(verification);

        String subject = "Email Verification OTP";
        String htmlBody = "<h3>Email Verification</h3>"
                + "<p>Thank you for registering. Please use the following One-Time Password (OTP) to complete your registration:</p>"
                + "<h2 style='color:#1a73e8; letter-spacing: 2px;'>" + otp + "</h2>"
                + "<p>This OTP is valid for <b>5 minutes</b>. Please do not share this code with anyone.</p>";

        emailService.sendEmail(email, subject, htmlBody);

        // send SMS too, if phone number provided
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            smsService.sendSms(phoneNumber, "Your OTP for Insurance Portal is " + otp + ". Valid for 5 minutes.");
        }
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

        otpRepository.delete(verification);
        log.info("OTP verified successfully for email: {}", email);
    }
}