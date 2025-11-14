package com.fa25se225.capstone.dto.v2.request;

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
    @NotBlank
    private String topicName;
    @NotBlank
    private String difficultyName;
    @NotBlank
    private String questionType;
    @NotNull
    private Integer numberOfQuestions;
    @NotNull
    private Double points;
}

