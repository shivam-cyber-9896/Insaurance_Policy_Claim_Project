package com.monocept.app.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.trim().isEmpty() && !"your_account_sid_here".equals(accountSid)) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio initialized successfully");
            } catch (Exception e) {
                log.error("Error initializing Twilio: {}", e.getMessage());
            }
        } else {
            log.warn("Twilio properties are not configured. SMS sending will be skipped.");
        }
    }

    @Async
    public void sendSms(String to, String messageBody) {
        if (accountSid == null || accountSid.trim().isEmpty() || "your_account_sid_here".equals(accountSid)) {
            log.error("Twilio credentials are not configured. SMS send skipped for: {}", to);
            return;
        }

        try {
            String formattedTo = to.trim();
            // Prepend +91 if it is a 10-digit number without country code
            if (formattedTo.length() == 10 && formattedTo.matches("\\d+")) {
                formattedTo = "+91" + formattedTo;
            }
            
            log.info("Sending SMS to {}", formattedTo);
            Message message = Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();
            log.info("SMS sent to {} successfully. SID: {}", formattedTo, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
        }
    }
}
