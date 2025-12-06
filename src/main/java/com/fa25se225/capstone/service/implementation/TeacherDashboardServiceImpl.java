package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.response.TeacherExamDashboardResponse;
import com.fa25se225.capstone.dto.response.TopExamTeacherStat;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.ExamTemplateV2Repository;
import com.fa25se225.capstone.service.TeacherDashboardService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherDashboardServiceImpl implements TeacherDashboardService {
    private final AccountUtil accountUtil;
    private final ExamTemplateV2Repository templateRepository;
    private final ExamAttemptV2Repository attemptRepository;

    @Override
    public TeacherExamDashboardResponse getExamDashboard() {
        User teacher = accountUtil.getCurrentUser();
        String teacherId = teacher.getId();

        Object[] stats = templateRepository.getTeacherAggregateStats(teacherId);

        long totalStudents = 0;
        double avgRating = 0.0;
        BigDecimal revenue = BigDecimal.ZERO;

        if (stats != null && stats.length >= 3) {
            totalStudents = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
            avgRating = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
            revenue = stats[2] != null ? new BigDecimal(stats[2].toString()) : BigDecimal.ZERO;
        }

        List<Object[]> rawTopExams = templateRepository.getTeacherTopExamsRaw(teacherId, PageRequest.of(0, 5));
        List<TopExamTeacherStat> topExams = rawTopExams.stream()
                .map(row -> {
                    String id = (String) row[0];
                    String title = (String) row[1];
                    Integer attempts = (Integer) row[2];
                    Double avgScore = (Double) row[3];
                    BigDecimal tokenCost = (BigDecimal) row[4];
                    BigDecimal calculatedRevenue = tokenCost.multiply(BigDecimal.valueOf(attempts))
                                                           .multiply(BigDecimal.valueOf(0.8));
                    return new TopExamTeacherStat(id, title, attempts, avgScore, calculatedRevenue);
                })
                .toList();

        long pendingCount = attemptRepository.countPendingReviewsByTeacher(teacherId);

        return TeacherExamDashboardResponse.builder()
                .totalStudentsTested(totalStudents)
                .totalExamAttempts(totalStudents)
                .estimatedRevenue(revenue)
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .topPerformingExams(topExams)
                .pendingManualReviews(pendingCount)
                .build();
    }
}
