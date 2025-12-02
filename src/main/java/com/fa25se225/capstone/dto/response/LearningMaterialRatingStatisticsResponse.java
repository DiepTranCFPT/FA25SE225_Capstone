package com.fa25se225.capstone.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningMaterialRatingStatisticsResponse {
    private String learningMaterialId;
    private Double averageRating;
    private Long totalRatings;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
}
