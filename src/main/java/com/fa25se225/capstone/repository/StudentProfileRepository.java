package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {
    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE u.email = :email")
    Optional<StudentProfile> findByUserEmail(String email);

    Optional<StudentProfile> findByUserId(String userId);
}