package com.fa25se225.capstone.dto.v2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class ExamQuestionV2Response {
    private String id;
    private QuestionV2Response question;
    private Integer orderNumber;
    private Double points;
}
