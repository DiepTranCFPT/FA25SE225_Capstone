package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @NonNull
    @Min(value = 0, message = "Token cost must greater than 0")
    private BigDecimal tokenCost;

    @NotEmpty
    @Valid
    private List<ExamRuleV2Request> rules;

    private Map<String, ScoreRange> scoreMapping;
}

