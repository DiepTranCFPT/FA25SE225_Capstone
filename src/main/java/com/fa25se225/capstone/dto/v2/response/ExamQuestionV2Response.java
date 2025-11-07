package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class ExamQuestionV2Response {
    private String examQuestionId;
    private QuestionV2Response question;
    private Integer orderNumber;
    private Double points;
}
