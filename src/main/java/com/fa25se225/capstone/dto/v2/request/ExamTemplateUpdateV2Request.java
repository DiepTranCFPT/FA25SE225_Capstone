package com.fa25se225.capstone.dto.v2.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    @Min(value = 0, message = "Token cost must greater than 0")
    private BigDecimal tokenCost;
    private Boolean isActive = false;
    private List<ExamRuleV2Request> rules;
    private Map<String, ScoreRange> scoreMapping;

}
