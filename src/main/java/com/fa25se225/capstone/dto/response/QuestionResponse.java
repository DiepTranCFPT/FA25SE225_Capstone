package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.constant.QuestionType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record QuestionResponse(
    String id,
    String content,
    String subject,
    QuestionType type,
    List<String> choices,
    Integer correctAnswer,
    String difficultyId,
    String difficultyName,
    String createdById,
    String createdByName,
    LocalDate createdAt,
    LocalDate updatedAt
) {}
