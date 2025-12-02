package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.TeacherRatingRequest;
import com.fa25se225.capstone.dto.request.UpdateTeacherRatingRequest;
import com.fa25se225.capstone.dto.response.TeacherRatingResponse;
import com.fa25se225.capstone.dto.response.TeacherRatingStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherRatingService {
    
   
    TeacherRatingResponse rateTeacher(TeacherRatingRequest request);
    
    TeacherRatingResponse updateRating(String ratingId, UpdateTeacherRatingRequest request);
    
   
    void deleteRating(String ratingId);
    
   
    Page<TeacherRatingResponse> getRatingsByTeacherId(String teacherId, Pageable pageable);
    
   
    Page<TeacherRatingResponse> getRatingsByStudentId(String studentId, Pageable pageable);
 
    TeacherRatingStatisticsResponse getTeacherRatingStatistics(String teacherId);
    
   
    TeacherRatingResponse getStudentRatingForTeacher(String teacherId, String studentId);
    
   
    TeacherRatingResponse verifyRating(String ratingId);
}
