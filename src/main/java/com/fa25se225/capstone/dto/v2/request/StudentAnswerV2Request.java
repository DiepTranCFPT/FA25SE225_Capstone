package com.fa25se225.capstone.dto.v2.request;

import lombok.Data;

@Data
public class StudentAnswerV2Request {
    private String examQuestionId;

    private String selectedAnswerId;

    private String frqAnswerText;
}
