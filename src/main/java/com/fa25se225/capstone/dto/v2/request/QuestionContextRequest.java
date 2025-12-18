package com.fa25se225.capstone.dto.v2.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContextRequest {
    private String title;
    private String content;
    private String imageUrl;
    private String audioUrl;
}
