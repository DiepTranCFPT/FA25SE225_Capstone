package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.kafka.FrqGradingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FrqGradingProducerService {

    private final KafkaTemplate<String, FrqGradingEvent> kafkaTemplate;
    private static final String GRADING_TOPIC = "frq_grading_tasks";

    public void sendGradingTask(FrqGradingEvent event){
        log.info("Sending FRQ grading task to topic '{}': studentAnswerId={}", GRADING_TOPIC, event.studentAnswerId());

        try {
            kafkaTemplate.send(GRADING_TOPIC, event.studentAnswerId(), event);
        } catch (Exception e) {
            log.error("Failed to send FRQ grading task to Kafka", e);
        }
    }



}
