package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TeacherRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRatingRepository extends JpaRepository<TeacherRating, String> {
    
   
    Page<TeacherRating> findByTeacherIdAndDeletedFalse(String teacherId, Pageable pageable);
    
    Page<TeacherRating> findByStudentIdAndDeletedFalse(String studentId, Pageable pageable);
    
    boolean existsByTeacherIdAndStudentIdAndDeletedFalse(String teacherId, String studentId);
    
    Optional<TeacherRating> findByTeacherIdAndStudentIdAndDeletedFalse(String teacherId, String studentId);
    
    Page<TeacherRating> findByLearningMaterialIdAndDeletedFalse(String materialId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM TeacherRating r WHERE r.teacher.id = :teacherId AND r.deleted = false")
    Double calculateAverageRating(@Param("teacherId") String teacherId);
    
    @Query("SELECT COUNT(r) FROM TeacherRating r WHERE r.teacher.id = :teacherId AND r.deleted = false")
    Long countRatingsByTeacherId(@Param("teacherId") String teacherId);
    
    Page<TeacherRating> findByTeacherIdAndIsVerifiedTrueAndDeletedFalse(String teacherId, Pageable pageable);
    
    @Query("SELECT r.rating, COUNT(r) FROM TeacherRating r WHERE r.teacher.id = :teacherId AND r.deleted = false GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("teacherId") String teacherId);
}
