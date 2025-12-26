package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.TeacherReviewResponse;

public interface TeacherReviewService {
     TeacherReviewResponse aiReviewTeacher(String userId);

    TeacherReviewResponse getLatestReviewByUserId(String userId);

}
