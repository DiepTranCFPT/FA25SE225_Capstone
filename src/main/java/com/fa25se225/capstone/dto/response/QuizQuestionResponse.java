package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizQuestionResponse {
    private String flashcardId;
    private String question;
    private String imageUrl;
    private List<String> options;
    private String correctAnswer;
}