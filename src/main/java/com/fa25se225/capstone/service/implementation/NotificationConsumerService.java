package com.fa25se225.capstone.service.implementation;


import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumerService {

    private final EmailService emailService;


    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void handleNotification(NotificationEvent event) {
        log.info("🐧🐧🐧 Received notification event: {}", event);

        switch (event.chanel().toUpperCase()) {
            case "EMAIL":
                try {
                    emailService.sendEmailWithTemplate(event.recipient(), event.templateCode(), event.params());
                } catch (Exception e) {
                    log.error("Failed to process email notification for recipient {}:", event.recipient(), e);
                }
                break;
            default:
                log.warn("Unknown notification channel: {}", event.chanel());
        }
    }

    @KafkaListener(topics = "message_test", groupId = "notification-group")
    public String handleMessage(String message) {
        log.info("🐧🐧🐧 Received notification event: {}", message);
        return message;


    }
}