package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ExamTemplateV2Repository extends JpaRepository<ExamTemplateV2, String>, JpaSpecificationExecutor<ExamTemplateV2> {
    Optional<ExamTemplateV2> findByTitle(String title);

    Page<ExamTemplateV2> findByCreatedById(String createdById, Pageable pageable);



}