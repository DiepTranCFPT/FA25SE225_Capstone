package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.dto.DashboardAdminResponse;
import com.fa25se225.capstone.dto.response.AdminUserDashboardResponse;
import com.fa25se225.capstone.dto.response.ExamDashboardResponse;
import com.fa25se225.capstone.dto.response.TopExamAdminStat;
import com.fa25se225.capstone.entity.AdminUserDailyStat;
import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.repository.AdminUserDailyStatRepository;
import com.fa25se225.capstone.repository.TokenTransactionRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.AdminDashboardService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final TokenTransactionRepository tokenTransactionRepository;
    private final AccountUtil accountUtil;


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



    @Override
    public DashboardAdminResponse getRevenueSystem(String day, String month, String year) {
        return getRevenueByType(day, month, year);
    }

    public DashboardAdminResponse getRevenueByType(String day, String month, String year) {
        User admin = accountUtil.getAccountAdmin();
        List<TokenTransaction> filtered;
        Long dayLong = null, monthLong = null, yearLong = null;
        try {
            // If all three are present and are numbers, construct date and filter by day
            if (day != null && month != null && year != null && day.matches("\\d{1,2}") && month.matches("\\d{1,2}") && year.matches("\\d{4}")) {
                int dayInt = Integer.parseInt(day);
                int monthInt = Integer.parseInt(month);
                int yearInt = Integer.parseInt(year);
                java.time.LocalDate localDate = java.time.LocalDate.of(yearInt, monthInt, dayInt);
                filtered = tokenTransactionRepository.findAllByUserAndCreatedAt(admin, localDate);
                dayLong = (long) dayInt;
                monthLong = (long) monthInt;
                yearLong = (long) yearInt;
            } else if (month != null && year != null && month.matches("\\d{1,2}") && year.matches("\\d{4}")) {
                int monthInt = Integer.parseInt(month);
                int yearInt = Integer.parseInt(year);
                filtered = tokenTransactionRepository.findAllByUserAndMonth(admin, yearInt, monthInt);
                monthLong = (long) monthInt;
                yearLong = (long) yearInt;
            } else if (year != null && year.matches("\\d{4}")) {
                int yearInt = Integer.parseInt(year);
                filtered = tokenTransactionRepository.findAllByUserAndYear(admin, yearInt);
                yearLong = (long) yearInt;
            } else if (day != null && day.matches("\\d{4}-\\d{2}-\\d{2}")) {
                java.time.LocalDate localDate = java.time.LocalDate.parse(day);
                filtered = tokenTransactionRepository.findAllByUserAndCreatedAt(admin, localDate);
                dayLong = (long) localDate.getDayOfMonth();
                monthLong = (long) localDate.getMonthValue();
                yearLong = (long) localDate.getYear();
            } else if (month != null && month.matches("\\d{4}-\\d{2}")) {
                String[] parts = month.split("-");
                int yearInt = Integer.parseInt(parts[0]);
                int monthInt = Integer.parseInt(parts[1]);
                filtered = tokenTransactionRepository.findAllByUserAndMonth(admin, yearInt, monthInt);
                monthLong = (long) monthInt;
                yearLong = (long) yearInt;
            } else {
                filtered = tokenTransactionRepository.findAllByUser(admin);
            }
        } catch (Exception e) {
            filtered = tokenTransactionRepository.findAllByUser(admin);
        }
        BigDecimal totalAmount = filtered.stream()
            .map(TokenTransaction::getAmount)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        DashboardAdminResponse response = new DashboardAdminResponse();
        response.setTotalAmount(totalAmount);
        response.setDay(dayLong);
        response.setMonth(monthLong);
        response.setYear(yearLong);
        return response;
    }
}
