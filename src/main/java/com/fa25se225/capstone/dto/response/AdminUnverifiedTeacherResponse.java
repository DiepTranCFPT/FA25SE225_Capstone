package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminUnverifiedTeacherResponse {
    private UserResponse user;
    private TeacherReviewResponse latestReview;
}
