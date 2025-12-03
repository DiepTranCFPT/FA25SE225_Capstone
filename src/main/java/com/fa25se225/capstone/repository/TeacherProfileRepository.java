package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, String> {
    Optional<TeacherProfile> findByUserId(String userId);
    Optional<TeacherProfile> findByIdAndDeletedFalse(String id);
}
