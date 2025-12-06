package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.dto.response.TopExamTeacherStat;
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

    @Query("SELECT t.id, t.title, t.totalTakers, t.averageRating, t.tokenCost " +
            "FROM ExamTemplateV2 t " +
            "WHERE t.createdBy.id = :teacherId " +
            "ORDER BY t.totalTakers DESC")
    List<Object[]> getTeacherTopExamsRaw(@Param("teacherId") String teacherId, Pageable pageable);

    @Query("SELECT " +
            "SUM(t.totalTakers), " +
            "AVG(t.averageRating), " +
            "SUM(t.totalTakers * t.tokenCost * 0.8) " +
            "FROM ExamTemplateV2 t " +
            "WHERE t.createdBy.id = :teacherId")
    Object[] getTeacherAggregateStats(@Param("teacherId") String teacherId);
}