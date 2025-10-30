package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, String> {
    @Query("SELECT q FROM Question q WHERE q.id = :id AND q.deleted = false")
    Optional<Question> findByIdNotDeleted(@Param("id") String id);
}

