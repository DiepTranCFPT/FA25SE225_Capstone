package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByUser(User user);
}
