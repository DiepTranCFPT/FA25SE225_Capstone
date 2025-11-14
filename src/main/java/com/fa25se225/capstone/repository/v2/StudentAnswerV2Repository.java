package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.StudentAnswerV2;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerV2Repository extends JpaRepository<StudentAnswerV2, String> {

    @Query("SELECT sa FROM StudentAnswerV2 sa " +
            "LEFT JOIN FETCH sa.selectedAnswer " +
            "WHERE sa.examAttempt.id = :attemptId")
    List<StudentAnswerV2> findByExamAttemptIdWithDetails(@Param("attemptId") String attemptId);
}
