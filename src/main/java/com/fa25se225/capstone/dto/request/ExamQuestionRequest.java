package com.fa25se225.capstone.dto.request;

import lombok.Data;

@Data
public class ExamQuestionRequest {
    private String examId;
    private String questionId;
    private Integer orderNumber;
    private Integer points;
}

