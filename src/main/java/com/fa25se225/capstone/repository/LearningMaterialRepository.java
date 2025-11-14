package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.LearningMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, String>, JpaSpecificationExecutor<LearningMaterial> {
    
    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.deleted = false")
    Page<LearningMaterial> findAllNotDeleted(Pageable pageable);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.id = :id AND lm.deleted = false")
    Optional<LearningMaterial> findByIdNotDeleted(@Param("id") String id);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.author.id = :authorId AND lm.deleted = false")
    Page<LearningMaterial> findByAuthorIdNotDeleted(@Param("authorId") String authorId, Pageable pageable);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.isPublic = true AND lm.deleted = false")
    Page<LearningMaterial> findAllPublicNotDeleted(Pageable pageable);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.subject.id = :subjectId AND lm.deleted = false")
    Page<LearningMaterial> findBySubjectIdNotDeleted(@Param("subjectId") String subjectId, Pageable pageable);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.type.id = :typeId AND lm.deleted = false")
    Page<LearningMaterial> findByTypeIdNotDeleted(@Param("typeId") String typeId, Pageable pageable);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE (LOWER(lm.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(lm.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND lm.deleted = false")
    Page<LearningMaterial> findByKeywordNotDeleted(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.deleted = false")
    List<LearningMaterial> findAllNotDeleted();

    @Query("SELECT lm FROM LearningMaterial lm " +
           "LEFT JOIN FETCH lm.type " +
           "LEFT JOIN FETCH lm.subject " +
           "LEFT JOIN FETCH lm.author " +
           "WHERE lm.title IN :titles AND lm.deleted = false")
    Page<LearningMaterial> findByTitlesNotDeleted(@Param("titles") List<String> titles, Pageable pageable);
}