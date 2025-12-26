package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.TeacherAiJson;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherReviewResponse;
import com.fa25se225.capstone.entity.TeacherProfile;
import com.fa25se225.capstone.entity.TeacherReview;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.TeacherReviewMapper;
import com.fa25se225.capstone.repository.TeacherProfileRepository;
import com.fa25se225.capstone.repository.TeacherReviewRepository;
import com.fa25se225.capstone.service.GeminiService;
import com.fa25se225.capstone.service.TeacherProfileService;
import com.fa25se225.capstone.service.TeacherReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class TeacherReviewServiceImpl implements TeacherReviewService {
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherReviewRepository teacherReviewRepository;
    private final TeacherReviewMapper teacherReviewMapper;
    private final TeacherProfileService teacherProfileService;
    private final GeminiService geminiService;

    public TeacherReviewServiceImpl(TeacherProfileRepository teacherProfileRepository,
                                TeacherReviewRepository teacherReviewRepository,
                                TeacherReviewMapper teacherReviewMapper,
                                TeacherProfileService teacherProfileService,
                                GeminiService geminiService) {
        this.teacherProfileRepository = teacherProfileRepository;
        this.teacherReviewRepository = teacherReviewRepository;
        this.teacherReviewMapper = teacherReviewMapper;
        this.teacherProfileService = teacherProfileService;
        this.geminiService = geminiService;
    }
    @Override
    @Transactional
    @Cacheable(cacheNames = "teacherReview", key = "#userId", sync = true)
    public TeacherReviewResponse aiReviewTeacher(String userId) {
        TeacherProfile profileEntity = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TeacherProfileResponse profile = teacherProfileService.getProfileByUserId(userId);

        TeacherAiJson ai = geminiService.reviewTeacherProfileForVerification(profile);

        teacherReviewRepository.markOldLatestFalse(profileEntity.getId());

        TeacherReview r = new TeacherReview();
        r.setTeacherProfile(profileEntity);
        r.setSyllabusAlignment(clamp1to5(ai.getSyllabusAlignment()));
        r.setConceptAccuracy(clamp1to5(ai.getConceptAccuracy()));
        r.setDifficultyFit(clamp1to5(ai.getDifficultyFit()));
        r.setExplanationQuality(clamp1to5(ai.getExplanationQuality()));
        r.setRecommendation(ai.getRecommendation());
        r.setFeedback(ai.getFeedback());
        r.setReviewerType("AI");
        r.setLatest(true);

        TeacherReview saved = teacherReviewRepository.save(r);
        return teacherReviewMapper.toResponse(saved);
    }

    @Override
    public TeacherReviewResponse getLatestReviewByUserId(String userId) {
        TeacherProfile profileEntity = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return teacherReviewRepository.findTopByTeacherProfileIdAndLatestTrue(profileEntity.getId())
                .map(teacherReviewMapper::toResponse)
                .orElse(null);
    }

    private int clamp1to5(Integer v) {
        if (v == null) return 1;
        if (v < 1) return 1;
        if (v > 5) return 5;
        return v;
    }
}
