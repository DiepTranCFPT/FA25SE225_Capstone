package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.service.StudentDashboardService;

import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.mapper.v2.ExamAttemptV2Mapper;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final AccountUtil accountUtil;
    private final ExamAttemptV2Repository attemptRepository;
    private final StudentAnswerV2Repository answerRepository;
    private final ExamAttemptV2Mapper attemptMapper;

    @Override
    public StudentExamDashboardResponse getStudentExamDashboard() {
        User student = accountUtil.getCurrentUser();
        String studentId = student.getId();

        long total = attemptRepository.countByUserIdAndStatus(studentId, AttemptStatusV2.COMPLETED);
        Double avgScore = attemptRepository.getAverageScoreByUserId(studentId);

        long inProgress = 0;

        List<Object[]> topicStats = answerRepository.analyzeTopicPerformance(studentId);
        Map<String, Double> topicPerformance = new HashMap<>();
        String weakestTopic = null;
        double minAcc = 100.0;

        for (Object[] row : topicStats) {
            String topicName = (String) row[0];
            long totalQ = (Long) row[1];
            long correctQ = (Long) row[2];

            double accuracy = totalQ > 0 ? (double) correctQ / totalQ * 100 : 0;
            topicPerformance.put(topicName, Math.round(accuracy * 10.0) / 10.0);

            if (accuracy < minAcc) {
                minAcc = accuracy;
                weakestTopic = topicName;
            }
        }

        var recentPage = attemptRepository.findByUserId(studentId, PageRequest.of(0, 5));
        var recentList = recentPage.getContent().stream().map(attemptMapper::toResponse).toList();

        return StudentExamDashboardResponse.builder()
                .totalExamsTaken(total)
                .averageScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0)
                .examsInProgress(inProgress)
                .topicPerformance(topicPerformance)
                .recommendedTopic(weakestTopic)
                .recentAttempts(recentList)
                .build();
    }

}
