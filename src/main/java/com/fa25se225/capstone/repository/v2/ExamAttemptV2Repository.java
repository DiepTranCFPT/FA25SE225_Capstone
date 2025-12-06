package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.dto.response.TopExamAdminStat;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptV2Repository extends JpaRepository<ExamAttemptV2, String> {

    Page<ExamAttemptV2> findByUserId(String userId, Pageable pageable);

    @Query("SELECT ea FROM ExamAttemptV2 ea " +
            "LEFT JOIN FETCH ea.user u " +
            "LEFT JOIN FETCH ea.exam e " +
            "LEFT JOIN FETCH ea.sourceTemplate " +
            "WHERE ea.id = :attemptId")
    Optional<ExamAttemptV2> findByIdWithDetails(@Param("attemptId") String attemptId);


    Optional<ExamAttemptV2> findFirstByUserIdAndSourceTemplateIdAndStatus(String userId, String templateId, AttemptStatusV2 status);



    @Query("SELECT ea FROM ExamAttemptV2 ea " +
            "JOIN ea.exam e " +
            "WHERE e.belongTo.id = :teacherId " +
            "AND ea.status IN :statuses")
    Page<ExamAttemptV2> findByTeacherAndStatusIn(@Param("teacherId") String teacherId, @Param("statuses") List<AttemptStatusV2> statuses, Pageable pageable);

    int countByUserIdAndStatus(String userId, AttemptStatusV2 status);

    @Query("SELECT AVG(ea.score) FROM ExamAttemptV2 ea WHERE ea.user.id = :userId AND ea.status = com.fa25se225.capstone.entity.v2.AttemptStatusV2.COMPLETED")
    Double getAverageScoreByUserId(@Param("userId") String userId);

    Optional<ExamAttemptV2> findFirstByUserIdAndStatusOrderByEndTimeDesc(String userId, AttemptStatusV2 status);

    long countByStatus(AttemptStatusV2 status);


    @Query("SELECT new com.fa25se225.capstone.dto.response.TopExamAdminStat(" +
            "t.id, t.title, u.email, COUNT(ea), AVG(ea.score)) " +
            "FROM ExamAttemptV2 ea " +
            "JOIN ea.sourceTemplate t " +
            "JOIN t.createdBy u " +
            "WHERE ea.status = com.fa25se225.capstone.entity.v2.AttemptStatusV2.COMPLETED " +
            "GROUP BY t.id, t.title, u.email " +
            "ORDER BY COUNT(ea) DESC")
    List<TopExamAdminStat> findTopPopularExams(Pageable pageable);

    @Query("SELECT COUNT(ea) FROM ExamAttemptV2 ea WHERE ea.status = 'REVIEW_REQUESTED'")
    long countReviewRequested();

    @Query("SELECT COUNT(ea) FROM ExamAttemptV2 ea " +
            "JOIN ea.sourceTemplate t " +
            "WHERE t.createdBy.id = :teacherId " +
            "AND ea.status = 'REVIEW_REQUESTED'")
    long countPendingReviewsByTeacher(@org.springframework.data.repository.query.Param("teacherId") String teacherId);
}
