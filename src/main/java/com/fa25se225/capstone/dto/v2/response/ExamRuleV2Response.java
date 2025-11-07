package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamRuleV2Response {
    private String id;
    private String topic;
    private String difficulty;
    private String questionType;
    private Integer numberOfQuestions;
    private Double points;
}

