package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionDifficultyV2Repository extends JpaRepository<QuestionDifficultyV2, String> {
}
