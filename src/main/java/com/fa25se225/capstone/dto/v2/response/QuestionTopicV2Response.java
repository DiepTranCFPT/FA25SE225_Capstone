package com.fa25se225.capstone.dto.v2.response;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
