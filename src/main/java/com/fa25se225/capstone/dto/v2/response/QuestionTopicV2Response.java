package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class QuestionTopicV2Response {
    private String id;
    private String name;
    private String subjectId;
    private String subjectName;
    private String description;
    private String createdBy;
    private LocalDateTime creatAt;
    private LocalDateTime updateAt;
}
