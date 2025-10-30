package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;

public record QuestionDifficultyCreationRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    String description
) {}
