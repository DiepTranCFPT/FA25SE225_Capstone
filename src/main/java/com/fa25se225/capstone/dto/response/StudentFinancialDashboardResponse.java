package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class StudentFinancialDashboardResponse {
    private BigDecimal totalSpent;
    private Long totalRegisteredMaterials;
    private BigDecimal currentBalance;
    private Map<String, BigDecimal> spendingBySubject;
    private Map<String, BigDecimal> spendingByType;
    private Map<String, BigDecimal> monthlySpending;
    private List<PurchasedMaterialInfo> recentPurchases;
}
