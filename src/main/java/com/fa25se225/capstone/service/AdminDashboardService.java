package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.DashboardAdminResponse;
import com.fa25se225.capstone.dto.response.AdminUserDashboardResponse;
import com.fa25se225.capstone.dto.response.ExamDashboardResponse;

public interface AdminDashboardService {
    AdminUserDashboardResponse getUserOverview();

    ExamDashboardResponse getExamAnalytics();

    DashboardAdminResponse getRevenueSystem(Long day, Long month, Long year);
}
