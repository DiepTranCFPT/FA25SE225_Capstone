package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ExamTemplateV2Repository extends JpaRepository<ExamTemplateV2, String> {
    Optional<ExamTemplateV2> findByTitle(String title);
}