package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.entity.AdminUserDailyStat;
import com.fa25se225.capstone.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsScheduler {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final AdminUserDailyStatRepository statRepository;

    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void aggregateUserStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting user stats aggregation for date: {}", yesterday);

        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);

        long newStudents = userRepository.countNewUsersByRole(PredefinedSystemRole.STUDENT.name(), start, end);
        long newTeachers = userRepository.countNewUsersByRole(PredefinedSystemRole.TEACHER.name(), start, end);

        long totalStudents = userRepository.countTotalByRole(PredefinedSystemRole.STUDENT.name());
        long totalTeachers = userRepository.countTotalByRole(PredefinedSystemRole.TEACHER.name());
        long totalParents = userRepository.countTotalByRole(PredefinedSystemRole.PARENT.name());
        long totalUsers = userRepository.count();

        long dau = loginHistoryRepository.countDistinctUsersLoginBetween(start, end);

        AdminUserDailyStat stat = AdminUserDailyStat.builder()
                .date(yesterday)
                .newStudents(newStudents)
                .newTeachers(newTeachers)
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalParents(totalParents)
                .totalUsers(totalUsers)
                .dailyActiveUsers(dau)
                .build();

        statRepository.save(stat);
        log.info("Aggregated stats saved for {}", yesterday);
    }
}