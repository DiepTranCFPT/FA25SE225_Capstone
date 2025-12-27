package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamTemplateV2Repository extends JpaRepository<ExamTemplateV2, String>, JpaSpecificationExecutor<ExamTemplateV2> {

    @Query("SELECT t FROM ExamTemplateV2 t WHERE t.id = :id AND t.deleted = false")
    Optional<ExamTemplateV2> findById(@Param("id") String id);

    @Query("SELECT t FROM ExamTemplateV2 t WHERE t.deleted = false")
    Page<ExamTemplateV2> findAll(Pageable pageable);

    @Query("SELECT t FROM ExamTemplateV2 t WHERE t.title = :title AND t.deleted = false")
    Optional<ExamTemplateV2> findByTitle(@Param("title") String title);

    @Query("SELECT t FROM ExamTemplateV2 t WHERE t.createdBy.id = :createdById AND t.deleted = false")
    Page<ExamTemplateV2> findByCreatedById(@Param("createdById") String createdById, Pageable pageable);

    @Query("SELECT t FROM ExamTemplateV2 t WHERE t.subject.id = :subjectId AND t.isActive = true AND t.deleted = false ORDER BY t.averageRating DESC")
    Page<ExamTemplateV2> findBySubjectIdAndIsActiveTrueOrderByAverageRatingDesc(@Param("subjectId") String subjectId, Pageable pageable);

    @Query("SELECT " +
            "t.id, " +
            "t.title, " +
            "COUNT(a), " +
            "COALESCE(AVG(a.score), 0.0), " +
            "t.tokenCost " +
            "FROM ExamAttemptV2 a " +
            "JOIN a.sourceTemplate t " +
            "WHERE t.createdBy.id = :teacherId AND a.status = com.fa25se225.capstone.entity.v2.AttemptStatusV2.COMPLETED " +
            "AND t.deleted = false " +
            "GROUP BY t.id, t.title, t.tokenCost " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> getTeacherTopExamsRaw(@Param("teacherId") String teacherId, Pageable pageable);

    @Query("SELECT " +
            "COUNT(DISTINCT a.user.id), " +
            "COALESCE(AVG(a.rating), 0.0), " +
            "COALESCE(SUM(a.sourceTemplate.tokenCost * 0.8), 0) " +
            "FROM ExamAttemptV2 a " +
            "WHERE a.sourceTemplate.createdBy.id = :teacherId AND a.status = com.fa25se225.capstone.entity.v2.AttemptStatusV2.COMPLETED " +
            "AND a.sourceTemplate.deleted = false")
    List<Object[]> getTeacherAggregateStats(@Param("teacherId") String teacherId);

    @Query("SELECT COUNT(a) FROM ExamAttemptV2 a WHERE a.sourceTemplate.createdBy.id = :teacherId AND a.sourceTemplate.deleted = false")
    long countTotalAttemptsByTeacher(@Param("teacherId") String teacherId);

    @Modifying
    @Query("UPDATE ExamTemplateV2 t SET t.deleted = true WHERE t.id = :id")
    void softDeleteById(@Param("id") String id);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM ExamTemplateV2 t WHERE t.id = :id AND t.deleted = false")
    boolean existsById(@Param("id") String id);
}
