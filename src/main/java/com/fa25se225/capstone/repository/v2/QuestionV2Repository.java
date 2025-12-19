
package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.constant.QuestionType;
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

    @Query("SELECT q FROM QuestionV2 q LEFT JOIN FETCH q.subject LEFT JOIN FETCH q.difficulty LEFT JOIN FETCH q.createdBy LEFT JOIN FETCH q.topic WHERE q.id = :id AND q.deleted = false")
    Optional<QuestionV2> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT q FROM QuestionV2 q WHERE q.subject.id = :subjectId AND q.deleted = false")
    Page<QuestionV2> findBySubjectId(@Param("subjectId") String subjectId, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.deleted = false")
    Page<QuestionV2> findAll(Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.topic.id = :topicId AND q.deleted = false")
    Page<QuestionV2> findByTopicId(@Param("topicId") String topicId, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.createdBy.id = :userId AND q.deleted = false")
    Page<QuestionV2> findByCreatedById(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.content LIKE %:keyword% AND q.deleted = false")
    Page<QuestionV2> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT q FROM QuestionV2 q WHERE q.subject.id = :subjectId AND q.difficulty.name = :difficultyName AND q.deleted = false")
    List<QuestionV2> findBySubjectIdAndDifficultyName(@Param("subjectId") String subjectId, @Param("difficultyName") String difficultyName);

    @Query("SELECT COUNT(q) FROM QuestionV2 q WHERE q.createdBy.id = :teacherId AND q.deleted = false")
    long countByCreatedById(@Param("teacherId") String teacherId);

    @Query("SELECT t.name, COUNT(q) FROM QuestionV2 q JOIN q.topic t WHERE q.createdBy.id = :teacherId AND q.deleted = false GROUP BY t.name")
    List<Object[]> countQuestionsByTopicForTeacher(@Param("teacherId") String teacherId);



    @Query(value = "SELECT * FROM questions_v2 q WHERE q.topic_id = :topicId AND q.question_type = :questionType " +
            "AND q.difficulty_id = :difficultyId AND q.deleted = false AND q.created_by = :creatorId ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<QuestionV2> findRandomQuestionsByCriteria(@Param("topicId") String topicId,@Param("questionType") String questionType,
                                                   @Param("difficultyId") String difficultyId, @Param("creatorId") String creatorId, @Param("count") int count);


    @Query(value = "SELECT * FROM questions_v2 q " +
            "WHERE q.topic_id = :topicId " +
            "AND q.difficulty_id = :difficultyId " +
            "AND q.question_type = :questionType " +
            "AND q.created_by = :creatorId " +
            "AND q.deleted = false " +
            "AND q.context_id IS NULL " +
            "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<QuestionV2> findRandomSingleQuestions(@Param("topicId") String topicId,
                                               @Param("questionType") String questionType,
                                               @Param("difficultyId") String difficultyId,
                                               @Param("creatorId") String creatorId,
                                               @Param("limit") int limit);

    @Query(value = "SELECT DISTINCT c.id FROM question_contexts_v2 c " +
            "JOIN questions_v2 q ON q.context_id = c.id " +
            "WHERE q.topic_id = :topicId " +
            "AND q.difficulty_id = :difficultyId " +
            "AND q.question_type = :questionType " +
            "AND q.created_by = :creatorId " +
            "AND q.deleted = false " +
            "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<String> findRandomContextIds(@Param("topicId") String topicId,
                                      @Param("questionType") String questionType,
                                      @Param("difficultyId") String difficultyId,
                                      @Param("creatorId") String creatorId,
                                      @Param("limit") int limit);


    @Query("SELECT COUNT(q) FROM QuestionV2 q WHERE " +
            "q.topic.id = :topicId AND " +
            "q.difficulty.id = :difficultyId AND " +
            "q.type = :questionType AND " +
            "q.createdBy.id = :creatorId AND " +
            "q.deleted = false")
    long countQuestionsByCriteria(
            @Param("topicId") String topicId,
            @Param("difficultyId") String difficultyId,
            @Param("questionType") QuestionType questionType,
            @Param("creatorId") String creatorId
    );

    @Query("SELECT s.name, COUNT(q) FROM QuestionV2 q JOIN q.subject s WHERE q.deleted = false GROUP BY s.name")
    List<Object[]> countQuestionsBySubject();

    @Query("SELECT d.name, COUNT(q) FROM QuestionV2 q JOIN q.difficulty d WHERE q.deleted = false GROUP BY d.name")
    List<Object[]> countQuestionsByDifficulty();

    @Query("SELECT q.id FROM QuestionV2 q " +
            "WHERE q.createdBy.id = :userId " +
            "AND q.deleted = false " +
            "AND EXISTS (" +
            "  SELECT 1 FROM QuestionV2 q2 " +
            "  WHERE q2.createdBy.id = :userId " +
            "  AND q2.deleted = false " +
            "  AND q.content LIKE CONCAT(CONCAT('%', q2.content), '%') " +
            "  AND (q2.createAt < q.createAt OR (q2.createAt = q.createAt AND q2.id < q.id))" +
            ")")
    List<String> findDuplicateQuestionIdsForUser(@Param("userId") String userId);
}
