package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.QuestionV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionV2Repository extends JpaRepository<QuestionV2, String> {

    @Query("SELECT q FROM QuestionV2 q LEFT JOIN FETCH q.subject LEFT JOIN FETCH q.difficulty LEFT JOIN FETCH q.createdBy LEFT JOIN FETCH q.topic WHERE q.id = :id")
    Optional<QuestionV2> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT q FROM QuestionV2 q WHERE q.subject.id = :subjectId")
    Page<QuestionV2> findBySubjectId(@Param("subjectId") String subjectId, Pageable pageable);


    @Query("SELECT q FROM QuestionV2 q WHERE q.topic.id = :topicId")
    Page<QuestionV2> findByTopicId(@Param("topicId") String topicId, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.createdBy.id = :userId")
    Page<QuestionV2> findByCreatedById(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.content LIKE %:keyword%")
    Page<QuestionV2> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.subject.id = :subjectId AND q.difficulty.name = :difficultyName")
    List<QuestionV2> findBySubjectIdAndDifficultyName(@Param("subjectId") String subjectId, @Param("difficultyName") String difficultyName);


    @Query(value = "SELECT * FROM questions_v2 q WHERE q.topic_id = :topicId AND q.question_type = :questionType " +
            "AND q.difficulty_id = :difficultyId AND q.created_by = :creatorId ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<QuestionV2> findRandomQuestionsByCriteria(@Param("topicId") String topicId,@Param("questionType") String questionType,
                                                   @Param("difficultyId") String difficultyId, @Param("creatorId") String creatorId, @Param("count") int count);

}
