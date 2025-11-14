package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionTopicV2CreationRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String subjectId;
    private String description;
}
