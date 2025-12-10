package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.DashboardAdminResponse;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.UserSearchRequest;
import com.fa25se225.capstone.dto.response.AdminUserDashboardResponse;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.ExamDashboardResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.service.AdminDashboardService;
import com.fa25se225.capstone.service.UserService;
// import com.fa25se225.capstone.service.AdminAnalyticsService; (Service thống kê dashboard nếu bạn đã làm)
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final AdminDashboardService adminAnalyticsService;

    @GetMapping("/stats")
    public ApiResponse<AdminUserDashboardResponse> getDashboardStats() {
        return ApiResponse.success(adminAnalyticsService.getUserOverview());
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<List<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean isLocked,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt:desc") String... sorts
    ) {
        UserSearchRequest searchRequest = new UserSearchRequest(keyword, role, isVerified, isLocked);
        return ApiResponse.success(userService.searchUsers(searchRequest, pageNo, pageSize, sorts));
    }

    @GetMapping("/exam-stats")
    public ApiResponse<ExamDashboardResponse> getExamStats() {
        return ApiResponse.success(adminAnalyticsService.getExamAnalytics());
    }

    @GetMapping("/revenue/system")
    public ApiResponse<DashboardAdminResponse> getRevenueSystem(
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year
    ) {
        return ApiResponse.success(adminAnalyticsService.getRevenueSystem(day, month, year));
    }
}