package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.PercentagesConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PercentagesConfigRepository extends JpaRepository<PercentagesConfig, String> {
    PercentagesConfig findByPercentTeacherVerified(BigDecimal percentTeacherVerified);
}

