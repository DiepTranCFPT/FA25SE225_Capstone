package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.LoginHistory;
import com.fa25se225.capstone.entity.User;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {
    List<LoginHistory> findByUserOrderByLoginTimeDesc(User user);

    @Query("SELECT h FROM LoginHistory h WHERE h.user = ?1 AND h.loginSuccess = false AND h.loginTime > ?2")
    List<LoginHistory> findFailedLoginAttemptsSince(User user, LocalDateTime since);

    @Query("SELECT h FROM LoginHistory h WHERE h.loginSuccess = false AND h.loginTime > ?1")
    List<LoginHistory> findAllFailedLoginAttemptsSince(LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT lh.user.id) FROM LoginHistory lh WHERE lh.loginTime BETWEEN :start AND :end")
    long countDistinctUsersLoginBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
