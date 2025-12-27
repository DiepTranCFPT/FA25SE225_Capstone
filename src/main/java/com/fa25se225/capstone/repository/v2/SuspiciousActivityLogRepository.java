package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.SuspiciousActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspiciousActivityLogRepository extends JpaRepository<SuspiciousActivityLog, String> {
    List<SuspiciousActivityLog> findByExamAttemptIdOrderByCreatedAtDesc(String attemptId);
}
