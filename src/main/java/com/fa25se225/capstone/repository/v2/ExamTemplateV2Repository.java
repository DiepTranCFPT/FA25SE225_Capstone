package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamTemplateV2Repository extends JpaRepository<ExamTemplateV2, String>, JpaSpecificationExecutor<ExamTemplateV2> {
    Optional<ExamTemplateV2> findByTitle(String title);

    Page<ExamTemplateV2> findByCreatedById(String createdById, Pageable pageable);

    Page<ExamTemplateV2> findBySubjectIdAndIsActiveTrueOrderByAverageRatingDesc(String subjectId, Pageable pageable);

    @Query("SELECT " +
            "t.id, " +
            "t.title, " +
            "COUNT(a), " +
            "COALESCE(AVG(a.score), 0.0), " +
            "t.tokenCost " +
            "FROM ExamAttemptV2 a " +
            "JOIN a.sourceTemplate t " +
            "WHERE t.createdBy.id = :teacherId AND a.status = com.fa25se225.capstone.entity.v2.AttemptStatusV2.COMPLETED " +
            "GROUP BY t.id, t.title, t.tokenCost " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> getTeacherTopExamsRaw(@Param("teacherId") String teacherId, Pageable pageable);

    @Query("SELECT " +
            "COUNT(DISTINCT a.user.id), " +
            "COALESCE(AVG(a.rating), 0.0), " +
            "COALESCE(SUM(a.sourceTemplate.tokenCost * 0.8), 0) " +
            "FROM ExamAttemptV2 a " +
            "WHERE a.sourceTemplate.createdBy.id = :teacherId AND a.status = com.fa25se225.capstone.entity.v2.AttemptStatusV2.COMPLETED")
    List<Object[]> getTeacherAggregateStats(@Param("teacherId") String teacherId);

    @Query("SELECT COUNT(a) FROM ExamAttemptV2 a WHERE a.sourceTemplate.createdBy.id = :teacherId")
    long countTotalAttemptsByTeacher(@Param("teacherId") String teacherId);
}
