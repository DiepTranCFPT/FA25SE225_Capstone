package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubjectCreationRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    String description
) {}
