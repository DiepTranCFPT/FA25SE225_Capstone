package com.fa25se225.capstone.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record QuestionResponse(
    String id,
    String content,
    List<String> choices,
    Integer correctAnswer,
    String difficultyId,
    String difficultyName,
    String createdById,
    String createdByName,
    LocalDate createdAt,
    LocalDate updatedAt
) {}
