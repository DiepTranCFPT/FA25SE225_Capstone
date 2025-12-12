package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentOverallDashboardResponse {
    private StudentExamDashboardResponse examStats;
    private StudentFinancialDashboardResponse financialStats;
}
