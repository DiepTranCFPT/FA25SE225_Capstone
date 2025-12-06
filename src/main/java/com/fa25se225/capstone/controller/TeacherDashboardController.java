package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.dto.response.TeacherExamDashboardResponse;
import com.fa25se225.capstone.service.StudentDashboardService;
import com.fa25se225.capstone.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/dashboard")
@RequiredArgsConstructor
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    @GetMapping("/exam-stats")
    public ApiResponse<TeacherExamDashboardResponse> getStudentDashboard() {
        return ApiResponse.success(teacherDashboardService.getExamDashboard());
    }
}
