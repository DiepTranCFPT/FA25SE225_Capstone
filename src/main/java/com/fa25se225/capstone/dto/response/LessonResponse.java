package com.fa25se225.capstone.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LessonResponse(
    String id,
    String name,
    String file,
    String url,
    String questionId,
    String description,
    String questionContent,
    String learningMaterialId,
    String learningMaterialTitle,
    LocalDate createdAt,
    LocalDate updatedAt,
    Integer lastWatchedSecond
) {}
