package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LearningMaterialCreationRequest(
    @NotBlank(message = "Title is required")
    String title,
    
    String description,
    
    @NotBlank(message = "Content URL is required")
    String contentUrl,
    
    @NotNull(message = "Material type ID is required")
    String typeId,
    
    String subjectId,
    
    Boolean isPublic
) {}