package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.entity.AdminUserDailyStat;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminUserDashboardResponse {
    private long totalUsers;
    private long totalStudents;
    private long totalTeachers;
    private long totalParents;

    private List<AdminUserDailyStat> chartData;
}