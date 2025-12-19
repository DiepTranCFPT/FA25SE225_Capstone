package com.fa25se225.capstone.dto.v2.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRuleV2Response {
    private String id;
    private String topic;
    private String difficulty;
    private String questionType;
    private Integer numberOfQuestions;
    private Integer numberOfContexts;
    private Double points;
}

