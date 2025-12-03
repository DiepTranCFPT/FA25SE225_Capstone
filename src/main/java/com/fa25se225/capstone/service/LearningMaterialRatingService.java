package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.LearningMaterialRatingRequest;
import com.fa25se225.capstone.dto.response.LearningMaterialRatingResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialRatingStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LearningMaterialRatingService {
    LearningMaterialRatingResponse rateLearningMaterial(LearningMaterialRatingRequest request);
    Page<LearningMaterialRatingResponse> getRatingsByMaterialId(String materialId, Pageable pageable);
    Page<LearningMaterialRatingResponse> getRatingsByUserId(String userId, Pageable pageable);
    LearningMaterialRatingStatisticsResponse getMaterialRatingStatistics(String materialId);
    LearningMaterialRatingResponse getUserRatingForMaterial(String materialId, String userId);
}
