package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.ExamQuestion;
import com.fa25se225.capstone.entity.v2.ExamQuestionV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamQuestionV2Repository extends JpaRepository<ExamQuestionV2, String> {
}
