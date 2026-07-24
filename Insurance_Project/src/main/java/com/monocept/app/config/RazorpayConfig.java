package com.monocept.app.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        String cleanKeyId = keyId != null ? keyId.trim() : "";
        String cleanKeySecret = keySecret != null ? keySecret.trim() : "";
        return new RazorpayClient(cleanKeyId, cleanKeySecret);
    }
}
