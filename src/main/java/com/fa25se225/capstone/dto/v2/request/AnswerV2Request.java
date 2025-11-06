package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerV2Request {

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Is correct flag is required")
    private Boolean isCorrect;

    private String explanation;
}
