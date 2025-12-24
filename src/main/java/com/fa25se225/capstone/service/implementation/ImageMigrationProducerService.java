package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.kafka.FlashcardImageMigrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMigrationProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final static String IMAGE_MIGRATION_TOPIC = "flashcard_image_migration";

    public void sendMigrationTask(String flashcardId, String originalUrl) {
        FlashcardImageMigrationEvent event = FlashcardImageMigrationEvent.builder()
                .flashcardId(flashcardId)
                .originalUrl(originalUrl)
                .build();

        log.info("Sending image migration task for Flashcard: {}", flashcardId);
        kafkaTemplate.send(IMAGE_MIGRATION_TOPIC, event);
    }
}