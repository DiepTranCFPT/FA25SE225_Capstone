package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDashboardResponse {
    private long totalAttempts;
    private long completedAttempts;
    private long pendingAttempts;
    private double completionRate;

    private long totalQuestions;
    private Map<String, Long> questionsBySubject;
    private Map<String, Long> questionsByDifficulty;

    private List<TopExamAdminStat> topPopularExams;

    private long manualReviewCount;
}
