package com.fa25se225.capstone.service.implementation;


import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationProducerService {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private static final String NOTIFICATION_TOPIC = "notifications";

    private final KafkaTemplate<String, String> kafkaString;
    private static final String MESSAGE_TEST_TOPIC = "message_test";


    public void sendNotification(NotificationEvent event) {
        log.info("Sending notification event to topic '{}': {}", NOTIFICATION_TOPIC, event);
        try {
            kafkaTemplate.send(NOTIFICATION_TOPIC, event);
        } catch (Exception e) {
            log.error("Failed to send notification event to Kafka", e);
        }
    }
    public void sendMessage(String message) {
        log.info("Sending notification event to topic '{}': {}", kafkaString, message);
        try {
            kafkaString.send(MESSAGE_TEST_TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to send notification event to Kafka", e);
        }
    }



}