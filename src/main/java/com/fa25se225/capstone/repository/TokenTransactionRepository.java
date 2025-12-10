package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.TokenTransactionType;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, String> {

    List<TokenTransaction> findAllByUser(User user);

    // Custom query for day
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TokenTransaction t WHERE t.createdAt = :date AND t.user = :user")
    List<TokenTransaction> findAllByUserAndCreatedAt(User user, java.time.LocalDate date);

    // Custom query for month
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TokenTransaction t WHERE FUNCTION('YEAR', t.createdAt) = :year AND FUNCTION('MONTH', t.createdAt) = :month AND t.user = :user")
    List<TokenTransaction> findAllByUserAndMonth(User user, int year, int month);

    // Custom query for year
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TokenTransaction t WHERE FUNCTION('YEAR', t.createdAt) = :year AND t.user = :user")
    List<TokenTransaction> findAllByUserAndYear(User user, int year);

}
