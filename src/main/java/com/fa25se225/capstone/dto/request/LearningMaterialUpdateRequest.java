package com.fa25se225.capstone.dto.request;

import java.math.BigDecimal;

public record LearningMaterialUpdateRequest(
    String title,
    String description,
    String contentUrl,
    String typeId,
    BigDecimal price,
    String subjectId,
    Boolean isPublic
) {}