package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.QuestionContextV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionContextV2Repository extends JpaRepository<QuestionContextV2, String> {
    Page<QuestionContextV2> findByCreatedById(String userId, Pageable pageable);

    Page<QuestionContextV2> findByCreatedByIdAndSubjectId(String userId, String subjectId, Pageable pageable);

   @Query("SELECT c.id FROM QuestionContextV2 c " +
                   "WHERE c.createdBy.id = :userId " +
                   "AND EXISTS (" +
                   "  SELECT 1 FROM QuestionContextV2 c2 " +
                   "  WHERE c2.createdBy.id = :userId " +
                   "  AND c.content LIKE CONCAT(CONCAT('%', c2.content), '%') " +
                   "  AND (c2.createdAt < c.createdAt OR (c2.createdAt = c.createdAt AND c2.id < c.id))" +
                   ")")
    List<String> findDuplicateContextIdsForUser(@Param("userId") String userId);

}
