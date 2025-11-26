package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.AdminUserDailyStat;
import com.fa25se225.capstone.service.implementation.AnalyticsScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdminUserDailyStatRepository extends JpaRepository<AdminUserDailyStat, String> {
    List<AdminUserDailyStat>  findAllByDateAfterOrderByDateAsc(LocalDate localDate);
}
