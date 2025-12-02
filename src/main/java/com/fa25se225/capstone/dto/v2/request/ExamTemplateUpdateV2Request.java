package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTemplateUpdateV2Request {
    private String title;
    private String description;
    private String subjectId;
    private Integer duration;
    private Integer passingScore;
    private BigDecimal tokenCost;
    private Boolean isActive = false;
    private List<ExamRuleV2Request> rules;
}

