package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.ExamQuestionV2;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionV2Repository extends JpaRepository<ExamQuestionV2, String> {

    @Query("SELECT eq FROM ExamQuestionV2 eq " +
            "JOIN FETCH eq.question q " +
            "JOIN FETCH q.topic " +
            "JOIN FETCH q.difficulty " +
            "WHERE eq.exam.id = :examId " +
            "ORDER BY eq.orderNumber ASC")
    List<ExamQuestionV2> findAllByExamIdWithDetails(@Param("examId") String examId);
}
