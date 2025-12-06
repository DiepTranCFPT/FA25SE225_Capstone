package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TeacherExamDashboardResponse {
    private long totalStudentsTested;
    private long totalExamAttempts;
    private BigDecimal estimatedRevenue;

    private long pendingManualReviews;
    private double averageRating;

    private List<TopExamTeacherStat> topPerformingExams;


}