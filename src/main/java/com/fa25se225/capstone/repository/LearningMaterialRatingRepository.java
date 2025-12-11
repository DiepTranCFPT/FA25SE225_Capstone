package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.LearningMaterialRating;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningMaterialRatingRepository extends JpaRepository<LearningMaterialRating, String> {

    Optional<LearningMaterialRating> findByLearningMaterialIdAndUserId(String learningMaterialId, String userId);

    Page<LearningMaterialRating> findByLearningMaterialIdAndDeletedFalse(String learningMaterialId, Pageable pageable);

    Page<LearningMaterialRating> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    Long countByLearningMaterialIdAndDeletedFalse(String learningMaterialId);

    @Query("SELECT AVG(r.rating) FROM LearningMaterialRating r WHERE r.learningMaterial.id = :materialId AND r.deleted = false")
    Double calculateAverageRating(@Param("materialId") String materialId);

    @Query("SELECT COUNT(r) FROM LearningMaterialRating r WHERE r.learningMaterial.id = :materialId AND r.rating = :rating AND r.deleted = false")
    Long countByLearningMaterialIdAndRating(@Param("materialId") String materialId, @Param("rating") Integer rating);

    List<LearningMaterialRating> findAllByUser(User user);

    List<LearningMaterialRating> findAllByLearningMaterial(LearningMaterial learningMaterial);
}
