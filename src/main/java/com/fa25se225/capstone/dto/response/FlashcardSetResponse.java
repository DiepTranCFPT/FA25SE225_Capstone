package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardSetResponse {
    private String id;
    private String title;
    private String description;
    private boolean visible;
    private int cardCount;
    private int viewCount;
    private UserResponse author;
    private LocalDateTime createdAt;
}