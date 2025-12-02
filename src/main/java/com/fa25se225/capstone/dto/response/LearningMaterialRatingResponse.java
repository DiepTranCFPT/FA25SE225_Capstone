package com.fa25se225.capstone.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningMaterialRatingResponse {
    private String id;
    private String learningMaterialId;
    private String learningMaterialTitle;
    private String studentId;
    private String studentName;
    private String userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
