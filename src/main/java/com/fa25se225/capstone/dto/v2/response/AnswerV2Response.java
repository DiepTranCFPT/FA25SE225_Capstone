package com.fa25se225.capstone.dto.v2.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerV2Response {
    private String id;
    private String content;
    private Boolean isCorrect;
    private String explanation;
}
