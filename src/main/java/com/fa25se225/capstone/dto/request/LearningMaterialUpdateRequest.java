package com.fa25se225.capstone.dto.request;

public record LearningMaterialUpdateRequest(
    String title,
    String description,
    String contentUrl,
    String typeId,
    String subjectId,
    Boolean isPublic
) {}