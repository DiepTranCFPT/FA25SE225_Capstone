package com.fa25se225.capstone.repository;



import com.fa25se225.capstone.constant.VerificationStatus;
import com.fa25se225.capstone.entity.TeacherVerificationRequest;
import com.fa25se225.capstone.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherVerificationRequestRepository extends JpaRepository<TeacherVerificationRequest, Long> {
    boolean existsByUserAndStatus(User user, VerificationStatus status);
    List<TeacherVerificationRequest> findByStatusOrderByCreatedAtAsc(VerificationStatus status);
    Optional<TeacherVerificationRequest> findByUserAndStatus(User user, VerificationStatus status);

    List<TeacherVerificationRequest> findByUserOrderByCreatedAtDesc(User user);
}

