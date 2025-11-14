package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamAttemptV2Repository extends JpaRepository<ExamAttemptV2, String> {

    Page<ExamAttemptV2> findByUserId(String userId, Pageable pageable);

    @Query("SELECT ea FROM ExamAttemptV2 ea " +
            "LEFT JOIN FETCH ea.user u " +
            "LEFT JOIN FETCH ea.exam e " +
            "LEFT JOIN FETCH ea.sourceTemplate " +
            "WHERE ea.id = :attemptId")
    Optional<ExamAttemptV2> findByIdWithDetails(@Param("attemptId") String attemptId);
}
