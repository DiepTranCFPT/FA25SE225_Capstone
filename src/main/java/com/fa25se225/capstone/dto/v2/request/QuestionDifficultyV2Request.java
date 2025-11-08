package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class QuestionDifficultyV2Request {
    @NotBlank
    private String name;
    private String description;
}
