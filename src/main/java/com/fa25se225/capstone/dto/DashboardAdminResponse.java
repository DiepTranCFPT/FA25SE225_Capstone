package com.fa25se225.capstone.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardAdminResponse {
    private BigDecimal totalAmount;
    private Long day;
    private Long month;
    private Long year;
}
