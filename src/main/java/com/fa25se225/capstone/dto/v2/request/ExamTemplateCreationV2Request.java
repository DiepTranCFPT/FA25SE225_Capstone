package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTemplateCreationV2Request {
    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String subject;
    @NotNull
    private Integer duration;
    @NotNull
    private Integer passingScore;
    private boolean isActive = false;
    @NotEmpty
    @Valid
    private List<ExamRuleV2Request> rules;
}

