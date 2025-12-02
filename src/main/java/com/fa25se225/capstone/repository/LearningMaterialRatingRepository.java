package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.LearningMaterialRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningMaterialRatingRepository extends JpaRepository<LearningMaterialRating, String> {

    Optional<LearningMaterialRating> findByLearningMaterialIdAndStudentId(String learningMaterialId, String studentId);

    Page<LearningMaterialRating> findByLearningMaterialIdAndDeletedFalse(String learningMaterialId, Pageable pageable);

    Page<LearningMaterialRating> findByStudentIdAndDeletedFalse(String studentId, Pageable pageable);

    Long countByLearningMaterialIdAndDeletedFalse(String learningMaterialId);

    @Query("SELECT AVG(r.rating) FROM LearningMaterialRating r WHERE r.learningMaterial.id = :materialId AND r.deleted = false")
    Double calculateAverageRating(@Param("materialId") String materialId);

    @Query("SELECT COUNT(r) FROM LearningMaterialRating r WHERE r.learningMaterial.id = :materialId AND r.rating = :rating AND r.deleted = false")
    Long countByLearningMaterialIdAndRating(@Param("materialId") String materialId, @Param("rating") Integer rating);
}
