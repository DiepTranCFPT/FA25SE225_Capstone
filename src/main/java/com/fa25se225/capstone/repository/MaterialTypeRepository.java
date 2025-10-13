package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MaterialTypeRepository extends JpaRepository<MaterialType, String> {
    
    @Query("SELECT mt FROM MaterialType mt WHERE mt.id = :id AND mt.deleted = false")
    Optional<MaterialType> findByIdNotDeleted(@Param("id") String id);
    
    @Query("SELECT mt FROM MaterialType mt WHERE mt.code = :code AND mt.deleted = false")
    Optional<MaterialType> findByCodeNotDeleted(@Param("code") String code);
}