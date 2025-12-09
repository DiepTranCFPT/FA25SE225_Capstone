package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.StudentExamDashboardResponse;
import com.fa25se225.capstone.dto.response.StudentFinancialDashboardResponse;
import com.fa25se225.capstone.dto.response.StudentOverallDashboardResponse;

public interface StudentDashboardService {
    StudentExamDashboardResponse getStudentExamDashboard();
    StudentExamDashboardResponse getChildrenExamDashboard(String childrenId);
    
    StudentFinancialDashboardResponse getStudentFinancialDashboard();
    StudentFinancialDashboardResponse getChildrenFinancialDashboard(String childrenId);
    
    StudentOverallDashboardResponse getStudentOverallDashboard();
}
