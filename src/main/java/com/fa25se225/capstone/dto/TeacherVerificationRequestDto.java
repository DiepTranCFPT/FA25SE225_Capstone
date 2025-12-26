package com.fa25se225.capstone.dto;

import com.fa25se225.capstone.constant.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherVerificationRequestDto {
    private String id;
    private String userId;
    private String teacherId;
    private VerificationStatus status;
    private String note;
    private String reviewedById;
    private Instant reviewedAt;
    private Instant createdAt;
    private Instant updatedAt;
}

