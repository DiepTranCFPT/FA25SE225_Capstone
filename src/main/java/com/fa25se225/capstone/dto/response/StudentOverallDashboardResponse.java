package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentOverallDashboardResponse {
    private StudentExamDashboardResponse examStats;
    private StudentFinancialDashboardResponse financialStats;
}
