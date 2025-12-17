package com.fa25se225.capstone.utils;

import com.fa25se225.capstone.dto.response.APResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class APScoreCalculator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public APResult calculate(Double rawScore, Double maxPossibleScore, String mappingJson) {
        if (rawScore == null) return new APResult(null, "Not graded yet");

        if (mappingJson == null || mappingJson.isEmpty()) {
            double percentage = (rawScore / maxPossibleScore) * 100.0;
            int score;
            if (percentage >= 85) score = 5;
            else if (percentage >= 70) score = 4;
            else if (percentage >= 55) score = 3;
            else if (percentage >= 40) score = 2;
            else score = 1;
            return new APResult(score, getMessage(score));
        }

        try {
            Map<String, Map<String, Double>> mapping = objectMapper.readValue(
                    mappingJson,
                    new TypeReference<>() {}
            );

            for (int i = 5; i >= 1; i--) {
                Map<String, Double> range = mapping.get(String.valueOf(i));
                if (range != null) {
                    Double min = range.get("min");
                    Double max = range.get("max");
                    if (rawScore >= min && rawScore <= max) {
                        return new APResult(i, getMessage(i));
                    }
                }
            }
            return new APResult(1, getMessage(1));
        } catch (Exception e) {
            log.error("Error parsing AP score mapping", e);
            return new APResult(null, "Error calculating score");
        }
    }

    private String getMessage(int score) {
        return switch (score) {
            case 5 -> "Extremely well qualified (A/A+)";
            case 4 -> "Very well qualified (A-/B+/B)";
            case 3 -> "Qualified (B-/C+/C)";
            case 2 -> "Possibly qualified";
            default -> "No recommendation";
        };
    }
}