package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, String> {
    
    @Query("SELECT s FROM Subject s WHERE s.id = :id AND s.deleted = false")
    Optional<Subject> findByIdNotDeleted(@Param("id") String id);
    
    @Query("SELECT s FROM Subject s WHERE s.code = :code AND s.deleted = false")
    Optional<Subject> findByCodeNotDeleted(@Param("code") String code);
}