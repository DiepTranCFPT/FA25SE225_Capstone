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
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
//controller này để test kafka thôi
public class EmailController {
    private final NotificationProducerService notificationProducerService;
    private final NotificationConsumerService notificationConsumerService;

    @PostMapping("/send-verification-email")
    public String testVerificationEmail(@RequestParam Set<String> emails) {
        log.info("Received request to test verification email for: {}", emails);

        Map<String, Object> emailParams = Map.of(
                "firstName", "Test User123",
                "verificationLink", "http://frontend.app..."
        );

        NotificationEvent event = NotificationEvent.builder()
                .chanel("EMAIL")
                .recipients(emails)
                .templateName("OTP_VERIFICATION")
                .params(emailParams)
                .build();

        notificationProducerService.sendNotification(event);

        return "Test email request sent to Kafka for: " + emails;
    }
}

