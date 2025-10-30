package com.fa25se225.capstone.dto.response;

import lombok.Data;

@Data
public class ExamQuestionResponse {
    private String id;
    private String examId;
    private String questionId;
    private Integer orderNumber;
    private Integer points;
}

