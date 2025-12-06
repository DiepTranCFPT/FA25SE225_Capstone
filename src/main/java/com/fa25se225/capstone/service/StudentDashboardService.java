package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;

public interface StudentDashboardService {
    StudentExamDashboardResponse getStudentExamDashboard();
    StudentExamDashboardResponse getChildrenExamDashboard(String childrenId);
}
