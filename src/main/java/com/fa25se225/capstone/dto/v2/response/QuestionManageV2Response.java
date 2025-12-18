package com.fa25se225.capstone.dto.v2.response;

import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionManageV2Response {
    private String id;
    private String content;
    private String type;
    private SubjectV2Response subject;
    private QuestionDifficultyV2Response difficulty;
    private String createdBy;
    private String topic;
    private List<AnswerV2Response> answers;
    private List<QuestionManageV2Response> subQuestions;
}
