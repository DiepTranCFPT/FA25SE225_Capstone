package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, String> {
    Optional<TransactionStatus> findByName(String name);

    Optional<TransactionStatus> findByCode(String code);
}
