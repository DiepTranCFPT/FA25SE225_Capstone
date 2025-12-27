package com.fa25se225.capstone.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectProgressDto {
    private String subjectId;
    private String subjectName;
    private long totalLessons;
    private long completedLessons;
    private boolean completed;

    public double getPercent() {
        if (totalLessons == 0) return 0;
        return (completedLessons * 100.0) / totalLessons;
    }
}

