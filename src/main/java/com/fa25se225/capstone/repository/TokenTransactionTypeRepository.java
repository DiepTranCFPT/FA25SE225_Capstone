package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TokenTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TokenTransactionTypeRepository extends JpaRepository<TokenTransactionType, Long> {
    Optional<TokenTransactionType> findByName(String name);
}
