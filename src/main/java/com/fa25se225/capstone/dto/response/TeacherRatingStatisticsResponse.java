package com.fa25se225.capstone.dto.response;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRatingStatisticsResponse {
    private String teacherId;
    private String teacherName;
    private Double averageRating;
    private Long totalRatings;
    private Map<Integer, Long> ratingDistribution; // e.g., {5: 10, 4: 5, 3: 2, 2: 1, 1: 0}
}
