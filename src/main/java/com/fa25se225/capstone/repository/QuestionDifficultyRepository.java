package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.QuestionDifficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionDifficultyRepository extends JpaRepository<QuestionDifficulty, String> {
    
    @Query("SELECT qd FROM QuestionDifficulty qd WHERE qd.id = :id AND qd.deleted = false")
    Optional<QuestionDifficulty> findByIdNotDeleted(@Param("id") String id);
    
    @Query("SELECT qd FROM QuestionDifficulty qd WHERE qd.deleted = false")
    Page<QuestionDifficulty> findAllNotDeleted(Pageable pageable);
}
