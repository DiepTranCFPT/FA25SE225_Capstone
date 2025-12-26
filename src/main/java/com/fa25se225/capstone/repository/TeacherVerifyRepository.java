package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TeacherVerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherVerifyRepository extends JpaRepository<TeacherVerificationRequest,String> {
}
