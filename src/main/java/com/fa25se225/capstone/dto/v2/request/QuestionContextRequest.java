package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContextRequest {
    @NotBlank(message = "Question context title must be filled")
    private String title;
    @NotBlank(message = "Question context content must be filled")
    private String content;
    private String imageUrl;
    private String audioUrl;
    @NotBlank(message = "Subject must be filled")
    private String subjectId;
}
