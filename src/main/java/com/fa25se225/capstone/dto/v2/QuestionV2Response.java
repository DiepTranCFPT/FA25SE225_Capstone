package com.fa25se225.capstone.dto.v2;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class QuestionV2Response {
    private String id;
    private String content;
    private String type;
    private SubjectV2Response subject;
    private QuestionDifficultyV2Response difficulty;
    private String createdBy;
    private String topic;
}
