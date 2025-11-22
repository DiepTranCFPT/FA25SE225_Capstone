package com.fa25se225.capstone.dto.v2.request;

import lombok.Data;
import java.util.List;

@Data
public class ManualGradeRequest {
    private List<GradeItem> grades;

    @Data
    public static class GradeItem {
        private String examQuestionId;
        private Double score;
        private String feedback;
    }
}