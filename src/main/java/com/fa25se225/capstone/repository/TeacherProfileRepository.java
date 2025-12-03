package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, String> {
    
    @Query("SELECT tp FROM TeacherProfile tp WHERE tp.user.id = :userId")
    Optional<TeacherProfile> findByUserId(@Param("userId") String userId);
    
    @Query(value = "SELECT * FROM teacher_profiles WHERE id = :id AND deleted = false", nativeQuery = true)
    Optional<TeacherProfile> findByIdAndDeletedFalse(@Param("id") String id);
    
    @Query(value = "SELECT * FROM teacher_profiles WHERE id = :id AND deleted = false", nativeQuery = true)
    Optional<TeacherProfile> findActiveTeacherById(@Param("id") String id);
}
