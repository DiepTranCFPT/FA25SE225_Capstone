package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TeacherReview;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherReviewRepository extends JpaRepository<TeacherReview, Long> {

    Optional<TeacherReview> findTopByTeacherProfileIdAndLatestTrue(String teacherProfileId);

    @Modifying
    @Query("""
        UPDATE TeacherReview tr
        SET tr.latest = false
        WHERE tr.teacherProfile.id = :profileId
          AND tr.latest = true
    """)
    int markOldLatestFalse(@Param("profileId") String profileId);
}
