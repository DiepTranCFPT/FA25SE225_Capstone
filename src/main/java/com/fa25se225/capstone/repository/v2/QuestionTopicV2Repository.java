package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionTopicV2Repository extends JpaRepository<QuestionTopicV2, String> {
    Optional<QuestionTopicV2> findByNameIgnoreCase(String name);

    @Query("SELECT qt FROM QuestionTopicV2 qt WHERE qt.subject.id = :subjectId")
    List<QuestionTopicV2> findBySubjectId(@Param("subjectId") String subjectId);

    @Query("SELECT qt FROM QuestionTopicV2 qt WHERE qt.createdBy.id = :userId")
    List<QuestionTopicV2> findByCreatedById(@Param("userId") String userId);

    boolean existsByNameIgnoreCaseAndSubjectIdAndCreatedById(String name, String subjectId, String createdById);
}
