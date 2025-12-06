package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.dto.response.AdminUserDashboardResponse;
import com.fa25se225.capstone.dto.response.ExamDashboardResponse;
import com.fa25se225.capstone.dto.response.TopExamAdminStat;
import com.fa25se225.capstone.entity.AdminUserDailyStat;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.repository.AdminUserDailyStatRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final AdminUserDailyStatRepository statRepository;
    private final ExamAttemptV2Repository attemptV2Repository;
    private final QuestionV2Repository questionV2Repository;


    @Override
    public AdminUserDashboardResponse getUserOverview() {
        long totalStudents = userRepository.countTotalByRole(PredefinedSystemRole.STUDENT.name());
        long totalTeachers = userRepository.countTotalByRole(PredefinedSystemRole.TEACHER.name());
        long totalParents = userRepository.countTotalByRole(PredefinedSystemRole.PARENT.name());

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<AdminUserDailyStat> chartData = statRepository.findAllByDateAfterOrderByDateAsc(thirtyDaysAgo);

        return AdminUserDashboardResponse.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalParents(totalParents)
                .totalUsers(userRepository.count())
                .chartData(chartData)
                .build();
    }

    public ExamDashboardResponse getExamAnalytics() {
        long totalAttempts = attemptV2Repository.count();
        long completed = attemptV2Repository.countByStatus(AttemptStatusV2.COMPLETED);
        long pending = attemptV2Repository.countByStatus(AttemptStatusV2.PENDING_GRADING);

        double rate = totalAttempts > 0 ? (double) completed / totalAttempts * 100 : 0;

        Map<String, Long> bySubject = questionV2Repository.countQuestionsBySubject().stream()
                .collect(Collectors.toMap(obj -> (String) obj[0], obj -> (Long) obj[1]));

        Map<String, Long> byDifficulty = questionV2Repository.countQuestionsByDifficulty().stream()
                .collect(Collectors.toMap(obj -> (String) obj[0], obj -> (Long) obj[1]));

        long totalQuestions = bySubject.values().stream().mapToLong(Long::longValue).sum();

        List<TopExamAdminStat> topExams = attemptV2Repository.findTopPopularExams(PageRequest.of(0, 5));

        long manualReviews = attemptV2Repository.countReviewRequested();

        return ExamDashboardResponse.builder()
                .totalAttempts(totalAttempts)
                .completedAttempts(completed)
                .pendingAttempts(pending)
                .completionRate(Math.round(rate * 100.0) / 100.0)
                .totalQuestions(totalQuestions)
                .questionsBySubject(bySubject)
                .questionsByDifficulty(byDifficulty)
                .topPopularExams(topExams)
                .manualReviewCount(manualReviews)
                .build();
    }
}
