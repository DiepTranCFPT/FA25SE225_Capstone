package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildOverviewResponse {
    private String studentId;
    private String studentName;
    private String email;
    private String avatarUrl;

    private long totalExamsTaken;
    private Double averageScore;
    private String lastExamTitle;
    private Double lastExamScore;
    private LocalDateTime lastActivity;
}