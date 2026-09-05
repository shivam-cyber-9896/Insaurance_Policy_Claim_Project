package com.monocept.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.monocept.app.dto.LoginRequestDto;
import com.monocept.app.dto.LoginResponseDto;
import com.monocept.app.dto.UserRequestDto;
import com.monocept.app.dto.UserResponseDto;
import com.monocept.app.dto.OtpRequestDto;
import com.monocept.app.dto.ApiResponse;
import com.monocept.app.service.AuthService;
import com.monocept.app.service.TokenBlacklistService;
import com.monocept.app.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRequestDto dto) {

        return ResponseEntity.ok(
                authService.register(dto));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody OtpRequestDto dto) {
        authService.verifyRegistration(dto);
        return ResponseEntity.ok(ApiResponse.success("Email OTP verified successfully."));
    }

    @PostMapping("/verify-mobile-otp")
    public ResponseEntity<ApiResponse<Void>> verifyMobileOtp(
            @Valid @RequestBody OtpRequestDto dto) {
        authService.verifyMobileRegistration(dto);
        return ResponseEntity.ok(ApiResponse.success("Mobile OTP verified successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTPs have been resent to your email and mobile."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto) {

        return ResponseEntity.ok(
                authService.login(dto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Verification code sent successfully."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        authService.resetPassword(email, otp, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Date expiration = jwtService.extractExpiration(token);
                LocalDateTime expiryDate = expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                tokenBlacklistService.blacklistToken(token, expiryDate);
            } catch (Exception e) {
                // If token is expired or malformed, it's already invalid
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out and token blacklisted successfully."));
    }
}
