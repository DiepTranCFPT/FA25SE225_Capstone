package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.LearningMaterialRatingRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialRatingResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialRatingStatisticsResponse;
import com.fa25se225.capstone.service.LearningMaterialRatingService;
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
@RequestMapping("/api/learning-material-ratings")
@RequiredArgsConstructor
public class LearningMaterialRatingController {
    
    private final LearningMaterialRatingService ratingService;

    @PostMapping
    public ApiResponse<LearningMaterialRatingResponse> rateLearningMaterial(@Valid @RequestBody LearningMaterialRatingRequest request) {
        log.info("Student rating learning material: {}", request.getLearningMaterialId());
        LearningMaterialRatingResponse response = ratingService.rateLearningMaterial(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/material/{materialId}")
    public ApiResponse<Page<LearningMaterialRatingResponse>> getRatingsByMaterial(
            @PathVariable String materialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LearningMaterialRatingResponse> ratings = ratingService.getRatingsByMaterialId(materialId, pageable);
        return ApiResponse.success(ratings);
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<Page<LearningMaterialRatingResponse>> getRatingsByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LearningMaterialRatingResponse> ratings = ratingService.getRatingsByStudentId(studentId, pageable);
        return ApiResponse.success(ratings);
    }

    @GetMapping("/material/{materialId}/statistics")
    public ApiResponse<LearningMaterialRatingStatisticsResponse> getMaterialStatistics(@PathVariable String materialId) {
        log.info("Getting rating statistics for material: {}", materialId);
        LearningMaterialRatingStatisticsResponse statistics = ratingService.getMaterialRatingStatistics(materialId);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/material/{materialId}/student/{studentId}")
    public ApiResponse<LearningMaterialRatingResponse> getStudentRatingForMaterial(
            @PathVariable String materialId,
            @PathVariable String studentId) {
        
        LearningMaterialRatingResponse rating = ratingService.getStudentRatingForMaterial(materialId, studentId);
        return ApiResponse.success(rating);
    }
}
