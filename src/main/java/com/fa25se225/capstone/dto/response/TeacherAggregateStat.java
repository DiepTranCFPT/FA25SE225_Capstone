package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TeacherAggregateStat {
    private Long totalStudents;
    private Double averageRating;
    private BigDecimal estimatedRevenue;
}