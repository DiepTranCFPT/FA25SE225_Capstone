package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.QuestionV2;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionV2Repository extends JpaRepository<QuestionV2, String> {

    @Query(value = "SELECT * FROM questions_v2 q WHERE q.topic_id = :topicId AND q.question_type = :questionType " +
            "AND q.difficulty_id = :difficultyId AND q.created_by = :creatorId ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<QuestionV2> findRandomQuestionsByCriteria(@Param("topicId") String topicId,@Param("questionType") String questionType,
                                                   @Param("difficultyId") String difficultyId, @Param("creatorId") String creatorId, @Param("count") int count);

    //    @Query(value = "SELECT * FROM questions_v2 q " +
//            "WHERE q.topic_id = :topicId " +
//            "AND q.difficulty_id = :difficultyName " +
//            "AND q.created_by = :teacherId " +
//            "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
//    List<QuestionV2> findRandomQuestionsByCriteria(
//            @Param("topicId") String topicId,
//            @Param("difficultyName") String difficultyName,
//            @Param("teacherId") String teacherId,
//            @Param("limit") int limit
//    );

}