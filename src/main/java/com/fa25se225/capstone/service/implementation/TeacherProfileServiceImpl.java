package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.TeacherProfileRequest;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.entity.TeacherProfile;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.TeacherProfileMapper;
import com.fa25se225.capstone.repository.TeacherProfileRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.TeacherProfileService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TeacherProfileServiceImpl implements TeacherProfileService {
    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherProfileMapper teacherProfileMapper;
    private final AccountUtil  accountUtil;
    private final UserRepository userRepository;



    @Override
    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = "user", key = "#result.userId")
    public TeacherProfileResponse createProfile(TeacherProfileRequest request) {
        validateAge(request.getDateOfBirth());
        User user = accountUtil.getCurrentUser();
        TeacherProfile profile = teacherProfileMapper.toEntity(request);
        profile.setUser(user);
        TeacherProfile saved = teacherProfileRepository.save(profile);
        return teacherProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    @Caching(evict = {
            @CacheEvict(value = "teacher_profile", key = "#id"),
            @CacheEvict(value = "user", key = "#result.userId")
    })
    public TeacherProfileResponse updateProfile(String id, TeacherProfileRequest request) {
        validateAge(request.getDateOfBirth());
        TeacherProfile profile = teacherProfileRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_PROFILE_NOT_FOUND));
        teacherProfileMapper.updateEntity(profile, request);
        TeacherProfile updated = teacherProfileRepository.save(profile);
        return teacherProfileMapper.toResponse(updated);
    }

    @Override
    public TeacherProfileResponse getProfileByUserId(String userId) {
        return teacherProfileRepository.findByUserId(userId)
            .map(teacherProfileMapper::toResponse)
            .orElseThrow(() -> new AppException(ErrorCode.TEACHER_PROFILE_NOT_FOUND));
    }
    private void validateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null || ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now()) < 23) {
            throw new AppException(ErrorCode.TEACHER_INVALID_AGE);
        }
    }
}
