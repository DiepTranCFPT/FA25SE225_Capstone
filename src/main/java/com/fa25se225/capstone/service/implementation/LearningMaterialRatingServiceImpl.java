package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.LearningMaterialRatingRequest;
import com.fa25se225.capstone.dto.response.LearningMaterialRatingResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialRatingStatisticsResponse;
import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.LearningMaterialRatingMapper;
import com.fa25se225.capstone.repository.*;
import com.fa25se225.capstone.service.CertificateService;
import com.fa25se225.capstone.service.LearningMaterialRatingService;
import com.fa25se225.capstone.service.NotificationService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningMaterialRatingServiceImpl implements LearningMaterialRatingService {
    
    private final LearningMaterialRatingRepository ratingRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final LearningMaterialRatingMapper ratingMapper;
    private final AccountUtil accountUtil;
    private final CertificateService certificateService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfile;
    private final ParentProfileRepository parentProfileRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public LearningMaterialRatingResponse rateLearningMaterial(LearningMaterialRatingRequest request) {
        User currentUser = accountUtil.getCurrentUser();
        
        LearningMaterial material = learningMaterialRepository.findById(request.getLearningMaterialId())
            .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
        
        Optional<LearningMaterialRating> existingRating = ratingRepository
            .findByLearningMaterialIdAndUserId(request.getLearningMaterialId(), currentUser.getId());
        
        if (existingRating.isPresent()) {
            throw new AppException(ErrorCode.LEARNING_MATERIAL_RATING_ALREADY_EXISTS);
        }
        
        LearningMaterialRating rating = LearningMaterialRating.builder()
            .learningMaterial(material)
            .user(currentUser)
            .rating(request.getRating())
            .comment(request.getComment())
            .build();
        
        rating = ratingRepository.save(rating);
        
        certificateService.createCertificate(currentUser, material.getAuthor(), material.getId());

        String message = currentUser.getFirstName() + " " + currentUser.getLastName() + "is complete learning material " + rating.getLearningMaterial().getTitle();
        notificationService.sendNotify(currentUser.getEmail(),"COMPLETE LEARNING",message);
        StudentProfile student = studentProfile.findAllByUser(currentUser);

        List<ParentProfile> parent = parentProfileRepository.findAllByChildren(List.of(student));
        for (ParentProfile parentProfile : parent) {
            notificationService.sendNotify(parentProfile.getUser().getEmail(),"COMPLETE LEARNING",message);
        }
        updateMaterialRatingCache(material.getId());
        
        return ratingMapper.toResponse(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningMaterialRatingResponse> getRatingsByMaterialId(String materialId, Pageable pageable) {
        Page<LearningMaterialRating> ratings = ratingRepository.findByLearningMaterialIdAndDeletedFalse(materialId, pageable);
        return ratings.map(ratingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningMaterialRatingResponse> getRatingsByUserId(String userId, Pageable pageable) {
        Page<LearningMaterialRating> ratings = ratingRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return ratings.map(ratingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningMaterialRatingStatisticsResponse getMaterialRatingStatistics(String materialId) {
        Double averageRating = ratingRepository.calculateAverageRating(materialId);
        Long totalRatings = ratingRepository.countByLearningMaterialIdAndDeletedFalse(materialId);
        
        Long fiveStarCount = ratingRepository.countByLearningMaterialIdAndRating(materialId, 5);
        Long fourStarCount = ratingRepository.countByLearningMaterialIdAndRating(materialId, 4);
        Long threeStarCount = ratingRepository.countByLearningMaterialIdAndRating(materialId, 3);
        Long twoStarCount = ratingRepository.countByLearningMaterialIdAndRating(materialId, 2);
        Long oneStarCount = ratingRepository.countByLearningMaterialIdAndRating(materialId, 1);
        
        return LearningMaterialRatingStatisticsResponse.builder()
            .learningMaterialId(materialId)
            .averageRating(averageRating != null ? averageRating : 0.0)
            .totalRatings(totalRatings)
            .fiveStarCount(fiveStarCount)
            .fourStarCount(fourStarCount)
            .threeStarCount(threeStarCount)
            .twoStarCount(twoStarCount)
            .oneStarCount(oneStarCount)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LearningMaterialRatingResponse getUserRatingForMaterial(String materialId, String userId) {
        LearningMaterialRating rating = ratingRepository.findByLearningMaterialIdAndUserId(materialId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_RATING_NOT_FOUND));
        
        return ratingMapper.toResponse(rating);
    }

    private void updateMaterialRatingCache(String materialId) {
        Double averageRating = ratingRepository.calculateAverageRating(materialId);
        Long totalRatings = ratingRepository.countByLearningMaterialIdAndDeletedFalse(materialId);
        
        LearningMaterial material = learningMaterialRepository.findById(materialId)
            .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
        
        material.setAverageRating(averageRating != null ? averageRating : 0.0);
        material.setTotalRatings(totalRatings != null ? totalRatings.intValue() : 0);
        
        learningMaterialRepository.save(material);
    }
}
