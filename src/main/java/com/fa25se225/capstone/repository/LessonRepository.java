package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {

    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN FETCH l.question " +
           "LEFT JOIN FETCH l.learningMaterial " +
           "WHERE l.deleted = false")
    Page<Lesson> findAllNotDeleted(Pageable pageable);

    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN FETCH l.question " +
           "LEFT JOIN FETCH l.learningMaterial " +
           "WHERE l.id = :id AND l.deleted = false")
    Optional<Lesson> findByIdNotDeleted(@Param("id") String id);

    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN FETCH l.question " +
           "LEFT JOIN FETCH l.learningMaterial " +
           "WHERE l.name LIKE %:name% AND l.deleted = false")
    Page<Lesson> findByNameContainingNotDeleted(@Param("name") String name, Pageable pageable);

    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN FETCH l.question " +
           "LEFT JOIN FETCH l.learningMaterial " +
           "WHERE l.learningMaterial.id = :learningMaterialId AND l.deleted = false")
    Page<Lesson> findByLearningMaterialIdNotDeleted(@Param("learningMaterialId") String learningMaterialId, Pageable pageable);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.question LEFT JOIN FETCH l.learningMaterial WHERE l.deleted = false")
    List<Lesson> findAllNotDeletedList();

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.question LEFT JOIN FETCH l.learningMaterial WHERE l.learningMaterial.id = :learningMaterialId AND l.deleted = false")
    List<Lesson> findByLearningMaterialIdNotDeletedList(@Param("learningMaterialId") String learningMaterialId);
}
