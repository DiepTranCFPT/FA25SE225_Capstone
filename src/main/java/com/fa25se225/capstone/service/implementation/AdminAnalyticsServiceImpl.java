package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.dto.response.AdminUserDashboardResponse;
import com.fa25se225.capstone.entity.AdminUserDailyStat;
import com.fa25se225.capstone.repository.AdminUserDailyStatRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final UserRepository userRepository;
    private final AdminUserDailyStatRepository statRepository;

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
}
