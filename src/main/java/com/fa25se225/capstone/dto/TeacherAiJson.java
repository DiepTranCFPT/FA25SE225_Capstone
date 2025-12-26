package com.fa25se225.capstone.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAiJson {
    private Integer syllabusAlignment;
    private Integer conceptAccuracy;
    private Integer difficultyFit;
    private Integer explanationQuality;
    private String recommendation;
    private String feedback;
}

