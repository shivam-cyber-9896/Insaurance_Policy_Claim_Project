package com.monocept.app.service.implementation;


import com.monocept.app.service.SmsService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @Override
	public void sendSms(String phoneNumber, String message) {
        Message.creator(
                new PhoneNumber("+91" + phoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
        ).create();
        log.info("SMS sent to {}", phoneNumber);
    }
}