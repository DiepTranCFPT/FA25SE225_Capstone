package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parent/dashboard")
@RequiredArgsConstructor
public class ParentDashboardController {
    private final StudentDashboardService studentDashboardService;

    @GetMapping("/exam-stats/{childrenId}")
    public ApiResponse<StudentExamDashboardResponse> getStudentDashboard(@PathVariable String childrenId) {
        return ApiResponse.success(studentDashboardService.getChildrenExamDashboard(childrenId));
    }
}
