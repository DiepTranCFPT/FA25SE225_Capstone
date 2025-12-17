package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class FlashcardSetDetailResponse {
    private String id;
    private String title;
    private String description;
    private Boolean isPublic;
    private int cardCount;
    private int viewCount;
    private UserResponse author;
    private LocalDateTime createdAt;
    private List<FlashcardResponse> flashcards;
}
