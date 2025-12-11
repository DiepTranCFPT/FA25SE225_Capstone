package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.TeacherRatingRequest;
import com.fa25se225.capstone.dto.request.UpdateTeacherRatingRequest;
import com.fa25se225.capstone.dto.response.TeacherRatingResponse;
import com.fa25se225.capstone.dto.response.TeacherRatingStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TeacherRatingService {
    
   
    TeacherRatingResponse rateTeacher(TeacherRatingRequest request);
    
    TeacherRatingResponse updateRating(String ratingId, UpdateTeacherRatingRequest request);
    
   
    void deleteRating(String ratingId);
    
   
    Page<TeacherRatingResponse> getRatingsByTeacherId(String teacherId, Pageable pageable);
 
    TeacherRatingStatisticsResponse getTeacherRatingStatistics(String teacherId);
    
    TeacherRatingResponse getUserRatingForTeacher(String teacherId, String userId);
    
   
    BigDecimal getAvgTeacherRating(String teacherId);
}
