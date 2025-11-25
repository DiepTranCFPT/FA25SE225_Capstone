package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, String> {
}

