package com.fa25se225.capstone.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherReviewResponse {
    private String id;
    private int syllabusAlignment;
    private int conceptAccuracy;
    private int difficultyFit;
    private int explanationQuality;
    private String recommendation;
    private String feedback;
    private String reviewerType;
    private boolean latest;
    private Instant createdAt;
}
