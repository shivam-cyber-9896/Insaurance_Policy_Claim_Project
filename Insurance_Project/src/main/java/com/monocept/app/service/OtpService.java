package com.monocept.app.service;



public interface OtpService {
    void sendOtp(String email, String phoneNumber);
    void verifyOtp(String email, String code);
    
}