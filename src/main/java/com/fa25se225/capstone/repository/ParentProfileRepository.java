package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.ParentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentProfileRepository extends JpaRepository<ParentProfile, String> {
    Optional<ParentProfile> findByUserId(String userId);
}