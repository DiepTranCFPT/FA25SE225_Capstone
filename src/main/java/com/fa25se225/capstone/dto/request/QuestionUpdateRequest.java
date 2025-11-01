package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.constant.QuestionType;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionUpdateRequest(
    String content,
    
    String subject,
    
    QuestionType type,
    
    @Size(min = 2, message = "At least 2 choices are required")
    List<String> choices,
    
    Integer correctAnswer,
    
    String difficultyId
) {}
