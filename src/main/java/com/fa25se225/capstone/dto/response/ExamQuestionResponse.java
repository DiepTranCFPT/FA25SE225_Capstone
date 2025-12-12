package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionResponse {
    private String id;
    private String examId;
    private String questionId;
    private Integer orderNumber;
    private Integer points;
}

