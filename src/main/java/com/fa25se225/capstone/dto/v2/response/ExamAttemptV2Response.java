package com.fa25se225.capstone.dto.v2.response;

import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptV2Response {
    private String attemptId;
    private String subject;
    private String title;
    private String examId;
    private String doneBy;
    private Double score;
    private Double passingScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String comment;
    private Integer rating;

}
