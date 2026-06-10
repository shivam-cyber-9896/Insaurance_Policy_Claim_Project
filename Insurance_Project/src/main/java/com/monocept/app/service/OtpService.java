package com.monocept.app.service;

public interface OtpService {
    void sendOtp(String email);
    void verifyOtp(String email, String code);
}
