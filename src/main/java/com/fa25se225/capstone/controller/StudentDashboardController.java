package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.dto.response.StudentFinancialDashboardResponse;
import com.fa25se225.capstone.dto.response.StudentOverallDashboardResponse;
import com.fa25se225.capstone.service.StudentDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/dashboard")
@RequiredArgsConstructor
@Tag(name = "Student Dashboard", description = "APIs for student dashboard statistics")
public class StudentDashboardController {
    private final StudentDashboardService studentDashboardService;

    @GetMapping("/exam-stats")
    @Operation(summary = "Get student exam statistics", 
               description = "Retrieves exam performance statistics for the current student")
    public ApiResponse<StudentExamDashboardResponse> getStudentDashboard() {
        return ApiResponse.success(studentDashboardService.getStudentExamDashboard());
    }
    
    @GetMapping("/financial-stats")
    @Operation(summary = "Get student financial statistics", 
               description = "Retrieves spending and learning materials statistics for the current student")
    public ApiResponse<StudentFinancialDashboardResponse> getStudentFinancialDashboard() {
        return ApiResponse.success(studentDashboardService.getStudentFinancialDashboard());
    }
    
    @GetMapping("/overall-stats")
    @Operation(summary = "Get overall student statistics", 
               description = "Retrieves combined exam and financial statistics for the current student")
    public ApiResponse<StudentOverallDashboardResponse> getStudentOverallDashboard() {
        return ApiResponse.success(studentDashboardService.getStudentOverallDashboard());
    }
}
