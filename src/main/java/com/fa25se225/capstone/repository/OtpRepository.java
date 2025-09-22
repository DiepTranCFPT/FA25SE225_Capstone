package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, String> {
    Optional<OtpEntity> findByEmailAndOtpAndUsedFalse(String email, String otp);

}
