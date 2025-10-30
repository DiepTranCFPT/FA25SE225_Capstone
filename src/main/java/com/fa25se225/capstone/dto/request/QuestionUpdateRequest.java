package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionUpdateRequest(
    String content,
    
    @Size(min = 2, message = "At least 2 choices are required")
    List<String> choices,
    
    Integer correctAnswer,
    
    String difficultyId
) {}
