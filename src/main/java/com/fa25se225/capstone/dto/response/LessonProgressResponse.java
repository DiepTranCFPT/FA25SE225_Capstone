package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record LessonProgressResponse(
    String id,
    String name,
    String file,
    String url,
    String questionId,
    String description,
    String questionContent,
    String learningMaterialId,
    String learningMaterialTitle,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Integer lastWatchedSecond,
    Boolean completed,
    Boolean isNextToContinue
) {}

