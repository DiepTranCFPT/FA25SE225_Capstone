package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByTeacher(User teacher);
    List<PaymentMethod> findAllByTeacher(User teacher);
}

