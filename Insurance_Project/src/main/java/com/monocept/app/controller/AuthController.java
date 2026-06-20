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

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}