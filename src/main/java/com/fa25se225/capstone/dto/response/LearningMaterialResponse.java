package com.fa25se225.capstone.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LearningMaterialResponse(
    String id,
    String title,
    String description,
    String contentUrl,
    String typeId,
    String typeName,
    String subjectId,
    String subjectName,
    String authorId,
    String authorName,
    BigDecimal price,
    Boolean isPublic,
    LocalDate createdAt,
    LocalDate updatedAt,
    String fileImage
) {}