package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.TeacherVerificationRequestDto;
import com.fa25se225.capstone.dto.request.TeacherProfileRequest;
import com.fa25se225.capstone.dto.response.AdminUnverifiedTeacherResponse;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherReviewResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.service.TeacherProfileService;
import com.fa25se225.capstone.service.TeacherReviewService;
import com.fa25se225.capstone.service.TeacherVerifyService;
import com.fa25se225.capstone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teacher-profile")
@RequiredArgsConstructor
public class TeacherProfileController {
    private final TeacherProfileService teacherProfileService;
    private final UserService userService;
    private final TeacherReviewService teacherReviewService;
    private final TeacherVerifyService teacherVerifyService;

    @PostMapping
    public ApiResponse<TeacherProfileResponse> createProfile(@RequestBody TeacherProfileRequest request) {
        log.info("Creating teacher profile with request: {}", request);
        TeacherProfileResponse response = teacherProfileService.createProfile(request);
        log.info("Created teacher profile: {}", response);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<TeacherProfileResponse> updateProfile(@PathVariable String id, @RequestBody TeacherProfileRequest request) {
        log.info("Updating teacher profile with id: {} and request: {}", id, request);
        TeacherProfileResponse response = teacherProfileService.updateProfile(id, request);
        log.info("Updated teacher profile: {}", response);
        return ApiResponse.success(response);
    }

    @GetMapping("/unverified")
    public ApiResponse<List<UserResponse>> getUnverifiedTeachers() {
        List<UserResponse> unverifiedTeachers = userService.getUnverifiedTeachers();
        return ApiResponse.success(unverifiedTeachers);
    }

    @PutMapping("/{id}/verify")
    public ApiResponse<UserResponse> verifyTeacher(@PathVariable String id) {
        UserResponse verifiedTeacher = userService.verifyTeacher(id);
        return ApiResponse.success(verifiedTeacher);
    }
    @PostMapping("/{userId}/ai-review")
    public TeacherReviewResponse aiReview(@PathVariable String userId) {
        return teacherReviewService.aiReviewTeacher(userId);
    }

    @GetMapping("/teacher/unverify")
    public ApiResponse<List<AdminUnverifiedTeacherResponse>> getUnverifiedTeachersByAdmin() {
        return ApiResponse.success(userService.getUnverifiedTeachersForAdmin());
    }

    @PostMapping("/request/verify/teacher")
    public ApiResponse<TeacherVerificationRequestDto> createRequestTeacherVerification() {
        return ApiResponse.success(teacherVerifyService.createRequestTeacherVerify());
    }
}
