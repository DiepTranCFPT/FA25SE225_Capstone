package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.dto.SubjectLessonCountProjection;
import com.fa25se225.capstone.entity.ParentProfile;
import com.fa25se225.capstone.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentProfileRepository extends JpaRepository<ParentProfile, String> {
    Optional<ParentProfile> findByUserId(String userId);

    List<ParentProfile> findAllByChildren(List<StudentProfile> children);

    @Query("""
  select (count(s) > 0)
  from  ParentProfile p join p.children s
  where p.id = :parentId and s.id = :studentId
""")
    boolean isParentOfStudent(String parentId, String studentId);

    @Query(value = """
    SELECT
        s.id AS subjectId,
        s.name AS subjectName,
        COUNT(DISTINCT l.id) AS totalLessons,
        COUNT(DISTINCT CASE WHEN slp.completed = 1 THEN l.id END) AS completedLessons
    FROM lessons l
    JOIN learning_materials lm ON lm.id = l.learning_material_id
    JOIN subjects s ON s.id = lm.subject_id
    JOIN lesson_video_progress slp
        ON slp.lesson_id = l.id
       AND slp.user_id = :studentId
    WHERE l.deleted = 0
      AND lm.deleted = 0
      AND s.deleted = 0
    GROUP BY s.id, s.name
""", nativeQuery = true)
    List<SubjectLessonCountProjection> getSubjectProgressForStudent(@Param("studentId") String studentId);

    @Query(value = """
        SELECT 
            s.id AS subjectId,
            s.name AS subjectName,
            COUNT(DISTINCT l.id) AS totalLessons,
            COUNT(DISTINCT CASE WHEN slp.completed = 1 THEN l.id END) AS completedLessons
        FROM lessons l 
        JOIN learning_materials lm ON lm.id = l.learning_material_id
        JOIN subjects s ON s.id = lm.subject_id
        JOIN lesson_video_progress slp 
            ON slp.lesson_id = l.id AND slp.user_id = :studentId
        WHERE l.deleted = false
          AND lm.deleted = false
          AND s.deleted = false
        GROUP BY s.id, s.name
        HAVING COUNT(DISTINCT l.id) = COUNT(DISTINCT CASE WHEN slp.completed = 1 THEN l.id END)
        """, nativeQuery = true)
    List<SubjectLessonCountProjection> getCompletedSubjectsForStudent(@Param("studentId") String studentId);


}