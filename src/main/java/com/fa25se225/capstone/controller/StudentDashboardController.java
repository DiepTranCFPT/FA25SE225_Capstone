package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {
    private final StudentDashboardService studentDashboardService;

    @GetMapping("/exam-stats")
    public ApiResponse<StudentExamDashboardResponse> getStudentDashboard() {
        return ApiResponse.success(studentDashboardService.getStudentExamDashboard());
    }
}
