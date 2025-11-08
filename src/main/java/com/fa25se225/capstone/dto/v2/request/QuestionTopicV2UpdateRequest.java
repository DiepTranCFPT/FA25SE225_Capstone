package com.fa25se225.capstone.dto.v2.request;

import lombok.Data;

@Data
public class QuestionTopicV2UpdateRequest {
    private String name;
    private String subjectId;
    private String description;
}
