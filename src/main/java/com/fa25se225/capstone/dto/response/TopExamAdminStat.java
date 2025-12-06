package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopExamAdminStat {
    private String templateId;
    private String title;
    private String teacherName;
    private long attemptCount;
    private double averageScore;
}
