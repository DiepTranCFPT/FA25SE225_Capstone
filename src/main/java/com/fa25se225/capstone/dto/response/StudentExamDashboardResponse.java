package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamDashboardResponse {
    private long totalExamsTaken;
    private double averageScore;
    private long examsInProgress;

    private Map<String, Double> topicPerformance;

    private String recommendedTopic;

    private List<ExamAttemptV2Response> recentAttempts;
}
