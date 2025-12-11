package com.fa25se225.capstone.dto.v2.response;

import lombok.*;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionV2Response {
    private String examQuestionId;
    private QuestionV2Response question;
    private Integer orderNumber;
    private Double points;

    private StudentAnswerDetailResponse savedAnswer;
}
