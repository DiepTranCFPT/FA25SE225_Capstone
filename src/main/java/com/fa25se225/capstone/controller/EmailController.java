package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.dto.response.EmailResponse;
import com.fa25se225.capstone.service.EmailService;
import com.fa25se225.capstone.service.implementation.NotificationConsumerService;
import com.fa25se225.capstone.service.implementation.NotificationProducerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailController {
    private final NotificationProducerService notificationProducerService;
    private final NotificationConsumerService notificationConsumerService;
    private final EmailService emailService;

    @PostMapping("/send-verification-email")
    public String testVerificationEmail(@RequestParam String email) {
        log.info("Received request to test verification email for: {}", email);

        Map<String, Object> emailParams = Map.of(
                "firstName", "Test User",
                "verificationLink", "http://yourapp.com/verify?token=dummy-test-token-12345"
        );

        NotificationEvent event = NotificationEvent.builder()
                .chanel("EMAIL")
                .recipient(email)
                .templateCode("VERIFY_EMAIL")
                .params(emailParams)
                .build();

        notificationProducerService.sendNotification(event);

        return "Test email request sent to Kafka for: " + email;
    }

    @PostMapping("/message")
    public String kafka(@RequestParam String message){
        notificationProducerService.sendMessage(message);
        return notificationConsumerService.handleMessage(message);
    }

    @PostMapping("/send-email")
    public EmailResponse sendEmail(@RequestParam String message){
        return emailService.sendEmailWithTemplate(
                "dovantri1709@gmail.com",
                "1",
                Map.of(
                        "firstName", "Test User",
                        "verificationLink", "http://yourapp.com/verify?token=dummy-test-token-12345"
                ));
    }
}

