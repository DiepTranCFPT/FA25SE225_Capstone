package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.TeacherProfileRequest;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;

public interface TeacherProfileService {
    TeacherProfileResponse createProfile(TeacherProfileRequest request);
    TeacherProfileResponse updateProfile(String id, TeacherProfileRequest request);
    TeacherProfileResponse getProfileByUserId(String userId);
}
