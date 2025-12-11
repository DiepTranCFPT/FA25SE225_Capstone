package com.fa25se225.capstone.dto.v2.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDifficultyV2Response {
    private String id;
    private String name;
    private String description;
}