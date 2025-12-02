package com.fa25se225.capstone.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRatingResponse {
    private String id;
    private String teacherId;
    private String teacherName;
    private String studentId;
    private String studentName;
    private String userId;
    private Integer rating;
    private String comment;
    private String learningMaterialId;
    private String learningMaterialTitle;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
