package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.Transaction;
import com.fa25se225.capstone.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByPayment_User(User user);
}
