package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopExamTeacherStat {
    private String templateId;
    private String title;
    private Long attempts;
    private Double avgScore;
    private BigDecimal revenue;
}
