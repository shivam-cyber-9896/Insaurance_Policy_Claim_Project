package com.monocept.app.service;

public interface OtpService {
    void sendOtp(String email);
    boolean verifyEmailOtp(String email, String code);
    boolean verifyMobileOtp(String email, String code);
}
