package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.TeacherRatingRequest;
import com.fa25se225.capstone.dto.request.UpdateTeacherRatingRequest;
import com.fa25se225.capstone.dto.response.TeacherRatingResponse;
import com.fa25se225.capstone.dto.response.TeacherRatingStatisticsResponse;
import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.TeacherRatingMapper;
import com.fa25se225.capstone.repository.*;
import com.fa25se225.capstone.service.TeacherRatingService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherRatingServiceImpl implements TeacherRatingService {
    
    private final TeacherRatingRepository teacherRatingRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final TeacherRatingMapper teacherRatingMapper;
    private final AccountUtil accountUtil;
    private final LearningMaterialRatingRepository learningMaterialRatingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public TeacherRatingResponse rateTeacher(TeacherRatingRequest request) {
        User currentUser = accountUtil.getCurrentUser();
        
        TeacherProfile teacher = teacherProfileRepository.findByIdAndDeletedFalse(request.getTeacherId())
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_PROFILE_NOT_FOUND));
        
        if (teacherRatingRepository.existsByTeacherIdAndUserIdAndDeletedFalse(
                request.getTeacherId(), currentUser.getId())) {
            throw new AppException(ErrorCode.TEACHER_RATING_ALREADY_EXISTS);
        }
        
        TeacherRating rating = TeacherRating.builder()
            .teacher(teacher)
            .user(currentUser)
            .rating(request.getRating())
            .comment(request.getComment())
            .isVerified(false)
            .build();
        
        if (request.getLearningMaterialId() != null) {
            LearningMaterial material = learningMaterialRepository
                .findByIdNotDeleted(request.getLearningMaterialId())
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
            rating.setLearningMaterial(material);
        }
        
        TeacherRating savedRating = teacherRatingRepository.save(rating);
        updateTeacherAverageRating(teacher.getId());
        
        log.info("User {} rated teacher {} with {} stars", 
            currentUser.getId(), teacher.getId(), request.getRating());
        
        return teacherRatingMapper.toResponse(savedRating);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public TeacherRatingResponse updateRating(String ratingId, UpdateTeacherRatingRequest request) {
        User currentUser = accountUtil.getCurrentUser();
        
        TeacherRating rating = teacherRatingRepository.findById(ratingId)
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_RATING_NOT_FOUND));
        
        if (!rating.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setIsVerified(false);
        
        TeacherRating updatedRating = teacherRatingRepository.save(rating);
        updateTeacherAverageRating(rating.getTeacher().getId());
        
        log.info("User {} updated rating {} for teacher {}", 
            currentUser.getId(), ratingId, rating.getTeacher().getId());
        
        return teacherRatingMapper.toResponse(updatedRating);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public void deleteRating(String ratingId) {
        User currentUser = accountUtil.getCurrentUser();
        
        TeacherRating rating = teacherRatingRepository.findById(ratingId)
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_RATING_NOT_FOUND));
        
        if (!rating.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        String teacherId = rating.getTeacher().getId();
        rating.setDeleted(true);
        teacherRatingRepository.save(rating);
        updateTeacherAverageRating(teacherId);
        
        log.info("User {} deleted rating {} for teacher {}", 
            currentUser.getId(), ratingId, teacherId);
    }

    @Override
    public Page<TeacherRatingResponse> getRatingsByTeacherId(String teacherId, Pageable pageable) {
        Page<TeacherRating> ratings = teacherRatingRepository
            .findByTeacherIdAndDeletedFalse(teacherId, pageable);
        return ratings.map(teacherRatingMapper::toResponse);
    }

    @Override
    public TeacherRatingStatisticsResponse getTeacherRatingStatistics(String teacherId) {
        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_PROFILE_NOT_FOUND));
        
        Double averageRating = teacherRatingRepository.calculateAverageRating(teacherId);
        Long totalRatings = teacherRatingRepository.countRatingsByTeacherId(teacherId);
        
        // Get rating distribution
        List<Object[]> distributionData = teacherRatingRepository.getRatingDistribution(teacherId);
        Map<Integer, Long> distribution = new HashMap<>();
        
        // Initialize all ratings (1-5) with 0
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        // Fill with actual data
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
        }
        
        String teacherName = teacher.getUser() != null ? 
            teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName() : null;
        
        return TeacherRatingStatisticsResponse.builder()
            .teacherId(teacherId)
            .teacherName(teacherName)
            .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
            .totalRatings(totalRatings)
            .ratingDistribution(distribution)
            .build();
    }

    @Override
    public TeacherRatingResponse getUserRatingForTeacher(String teacherId, String userId) {
        TeacherRating rating = teacherRatingRepository
            .findByTeacherIdAndUserIdAndDeletedFalse(teacherId, userId)
            .orElse(null);
        
        return rating != null ? teacherRatingMapper.toResponse(rating) : null;
    }

    @Override
    public BigDecimal getAvgTeacherRating(String teacherId) {
        List<LearningMaterial> list = learningMaterialRepository.findByAuthor(teacherId);

        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double avgOfAllMaterials = list.stream()
                .mapToDouble(lm -> {
                    List<LearningMaterialRating> rate =
                            learningMaterialRatingRepository.findAllByLearningMaterial(lm);

                    return rate.stream()
                            .map(LearningMaterialRating::getRating)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0);
                })
                .average()
                .orElse(0.0);


        return BigDecimal.valueOf(avgOfAllMaterials);
    }




    private void updateTeacherAverageRating(String teacherId) {
        Double averageRating = teacherRatingRepository.calculateAverageRating(teacherId);
        Long totalRatings = teacherRatingRepository.countRatingsByTeacherId(teacherId);
        
        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_PROFILE_NOT_FOUND));
        
        teacher.setRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : null);
        teacher.setTotalRatings(totalRatings.intValue());
        
        teacherProfileRepository.save(teacher);
        
        log.info("Updated teacher {} average rating to {} (total: {})", 
            teacherId, teacher.getRating(), totalRatings);
    }
}
