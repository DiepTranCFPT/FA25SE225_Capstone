package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, String> {

    @Query("SELECT e FROM Exam e WHERE e.id = :id AND e.deleted = false")
    Exam findByIdNotDeleted(@Param("id") String id);

    @Query("SELECT e FROM Exam e WHERE e.deleted = false")
    List<Exam> findAllNotDeleted();

    @Query("SELECT e FROM Exam e WHERE e.createdBy.id = :userId AND e.deleted = false")
    List<Exam> findAllByUserIdNotDeleted(@Param("userId") String userId);

}
