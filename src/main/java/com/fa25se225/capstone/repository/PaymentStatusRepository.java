package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, String> {
    Optional<PaymentStatus> findByCode(String code);
    Optional<PaymentStatus> findByName(String name);
}

