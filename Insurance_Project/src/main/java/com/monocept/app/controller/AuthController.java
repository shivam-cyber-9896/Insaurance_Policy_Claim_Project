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
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully. Your account is now active."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP has been resent to your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto) {

        return ResponseEntity.ok(
                authService.login(dto));
    }
}