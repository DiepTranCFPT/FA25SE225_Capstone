package com.fa25se225.capstone.dto.v2.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionContextV2Response {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private String audioUrl;
}
