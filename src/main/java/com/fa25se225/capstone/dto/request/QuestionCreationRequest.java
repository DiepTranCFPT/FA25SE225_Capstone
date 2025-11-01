package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.constant.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionCreationRequest(
    @NotBlank(message = "Question content is required")
    String content,
    
    String subject,
    
    @NotNull(message = "Question type is required")
    QuestionType type,
    
    @NotNull(message = "Choices are required")
    @Size(min = 2, message = "At least 2 choices are required")
    List<String> choices,
    
    @NotNull(message = "Correct answer is required")
    Integer correctAnswer,
    
    @NotNull(message = "Difficulty ID is required")
    String difficultyId
) {}
