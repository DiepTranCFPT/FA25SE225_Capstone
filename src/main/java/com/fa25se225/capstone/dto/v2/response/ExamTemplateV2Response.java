package com.fa25se225.capstone.dto.v2.response;

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
public class ExamTemplateV2Response {
    private String id;
    private String title;
    private String description;
    private SubjectV2Response subject;
    private Integer duration;
    private Integer passingScore;
    private Boolean isActive;
    private String createdBy;
    private List<ExamRuleV2Response> rules;
    private BigDecimal tokenCost;

    private Double averageRating;
    private Integer totalRatings;
    private Integer totalTakers;
}

