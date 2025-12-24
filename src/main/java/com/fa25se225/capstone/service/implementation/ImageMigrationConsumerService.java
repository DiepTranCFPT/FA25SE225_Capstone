package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.kafka.FlashcardImageMigrationEvent;
import com.fa25se225.capstone.dto.kafka.FrqGradingEvent;
import com.fa25se225.capstone.entity.flashcard.Flashcard;
import com.fa25se225.capstone.entity.v2.StudentAnswerV2;
import com.fa25se225.capstone.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMigrationConsumerService {

    private final CloudinaryService cloudinaryService;
    private final FlashcardRepository flashcardRepository;
    private final static String FLASHCARD_IMAGES_FOLDER = "flashcard_images_posts";


    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            include = {RuntimeException.class}
    )
    @KafkaListener(topics = "flashcard_image_migration", groupId = "image-migration-group")
    public void handleImageMigration(FlashcardImageMigrationEvent event) {
        log.info("Processing migration for Flashcard ID: {}", event.getFlashcardId());

        try {
            Flashcard flashcard = flashcardRepository.findById(event.getFlashcardId()).orElse(null);
            if (flashcard == null) return;

            String permanentUrl = cloudinaryService.uploadFileFromUrl(event.getOriginalUrl(), FLASHCARD_IMAGES_FOLDER);

            flashcard.setImageUrl(permanentUrl);
            flashcardRepository.save(flashcard);

            log.info("Successfully migrated image. New URL: {}", permanentUrl);

        } catch (Exception e) {
            log.error("Failed to migrate image for Flashcard ID: {}", event.getFlashcardId(), e);
        }
    }

    @DltHandler
    @Transactional
    public void handleDlt(FlashcardImageMigrationEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Migrate image failed for flashcard: {} after retries. Moving to Manual Review.", event.getFlashcardId());
    }
}