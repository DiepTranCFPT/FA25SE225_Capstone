package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.TeacherRatingRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.TeacherRatingResponse;
import com.fa25se225.capstone.dto.response.TeacherRatingStatisticsResponse;
import com.fa25se225.capstone.service.TeacherRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/teacher-ratings")
@RequiredArgsConstructor
public class TeacherRatingController {
    
    private final TeacherRatingService teacherRatingService;

    @PostMapping
    public ApiResponse<TeacherRatingResponse> rateTeacher(@Valid @RequestBody TeacherRatingRequest request) {
        log.info("Student rating teacher: {}", request.getTeacherId());
        TeacherRatingResponse response = teacherRatingService.rateTeacher(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<Page<TeacherRatingResponse>> getRatingsByTeacher(
            @PathVariable String teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TeacherRatingResponse> ratings = teacherRatingService.getRatingsByTeacherId(teacherId, pageable);
        return ApiResponse.success(ratings);
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<Page<TeacherRatingResponse>> getRatingsByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TeacherRatingResponse> ratings = teacherRatingService.getRatingsByStudentId(studentId, pageable);
        return ApiResponse.success(ratings);
    }

    @GetMapping("/teacher/{teacherId}/statistics")
    public ApiResponse<TeacherRatingStatisticsResponse> getTeacherStatistics(@PathVariable String teacherId) {
        log.info("Getting rating statistics for teacher: {}", teacherId);
        TeacherRatingStatisticsResponse statistics = teacherRatingService.getTeacherRatingStatistics(teacherId);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/teacher/{teacherId}/student/{studentId}")
    public ApiResponse<TeacherRatingResponse> getStudentRatingForTeacher(
            @PathVariable String teacherId,
            @PathVariable String studentId) {
        
        TeacherRatingResponse rating = teacherRatingService.getStudentRatingForTeacher(teacherId, studentId);
        return ApiResponse.success(rating);
    }
}
