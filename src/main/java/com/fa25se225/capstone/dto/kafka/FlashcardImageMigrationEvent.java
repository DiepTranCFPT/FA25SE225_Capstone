package com.fa25se225.capstone.dto.kafka;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardImageMigrationEvent {
    private String flashcardId;
    private String originalUrl;
}
