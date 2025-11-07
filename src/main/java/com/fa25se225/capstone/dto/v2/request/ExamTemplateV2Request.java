package com.fa25se225.capstone.dto.v2.request;

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
public class ExamTemplateV2Request {
    @NotBlank
    private String title;
    private String description;

    @NotBlank(message = "Subject ID is required")
    private String subjectId;
    @NotNull
    private Integer duration;
    @NotNull
    private Integer passingScore;
    private Boolean isActive = true;
    private List<ExamRuleV2Request> rules;
}

