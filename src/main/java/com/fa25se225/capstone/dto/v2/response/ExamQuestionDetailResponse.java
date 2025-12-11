package com.fa25se225.capstone.dto.v2.response;

import com.fa25se225.capstone.dto.v2.response.QuestionV2Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDetailResponse {
    private String examQuestionId;
    private QuestionV2Response question;
    private Integer orderNumber;
    private Double points;
    private StudentAnswerDetailResponse studentAnswer;
}