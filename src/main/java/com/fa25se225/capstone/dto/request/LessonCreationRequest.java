package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LessonCreationRequest(
    @NotBlank(message = "Name is required")
    String name,

    String url,

    String questionId,

    String description,

    String learningMaterialId
) {}
