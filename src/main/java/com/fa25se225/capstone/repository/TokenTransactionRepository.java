package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, String> {

    List<TokenTransaction> findAllByUser(User user);

    @Query("SELECT t FROM TokenTransaction t WHERE t.createdAt = :date AND t.user = :user")
    List<TokenTransaction> findAllByUserAndCreatedAt(User user, java.time.LocalDate date);

    @Query("SELECT t FROM TokenTransaction t WHERE FUNCTION('YEAR', t.createdAt) = :year AND FUNCTION('MONTH', t.createdAt) = :month AND t.user = :user")
    List<TokenTransaction> findAllByUserAndMonth(User user, int year, int month);

    @Query("SELECT t FROM TokenTransaction t WHERE FUNCTION('YEAR', t.createdAt) = :year AND t.user = :user")
    List<TokenTransaction> findAllByUserAndYear(User user, int year);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.amount), 0) FROM TokenTransaction t WHERE t.user = :user AND t.status = 'pending'")
    BigDecimal sumPendingAmountByUser(com.fa25se225.capstone.entity.User user);

}
