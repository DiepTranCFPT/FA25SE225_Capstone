package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherExamDashboardResponse {
    private long totalStudentsTested;
    private long totalExamAttempts;
    private BigDecimal estimatedRevenue;

    private long pendingManualReviews;
    private double averageRating;

    private long totalQuestions;
    private Map<String, Long> questionsByTopic;

    private List<TopExamTeacherStat> topPerformingExams;
}
