package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRuleV2Request {
    @NotBlank(message = "Topic Name is required")
    private String topicName;
    @NotBlank(message = "Difficulty Name is required")
    private String difficultyName;
    @NotBlank(message = "Question Type is required. MCQ/FRQ")
    private String questionType;
    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "Number of questions must be at least 1")
    private Integer numberOfQuestions;
    @NotNull
    @Min(value = 0, message = "Point cannot be negative")
    private Double points;

    @Min(value = 0, message = "Number of contexts cannot be negative")
    private Integer numberOfContexts = 0;
}

