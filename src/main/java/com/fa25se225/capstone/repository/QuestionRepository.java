package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, String> {
    
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.difficulty " +
           "LEFT JOIN FETCH q.createdBy " +
           "WHERE q.deleted = false")
    Page<Question> findAllNotDeleted(Pageable pageable);
    
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.difficulty " +
           "LEFT JOIN FETCH q.createdBy " +
           "WHERE q.id = :id AND q.deleted = false")
    Optional<Question> findByIdNotDeleted(@Param("id") String id);
    
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.difficulty " +
           "LEFT JOIN FETCH q.createdBy " +
           "WHERE q.createdBy.id = :teacherId AND q.deleted = false")
    Page<Question> findByCreatedByIdNotDeleted(@Param("teacherId") String teacherId, Pageable pageable);
}
