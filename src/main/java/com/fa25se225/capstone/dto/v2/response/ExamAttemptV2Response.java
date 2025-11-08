package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamAttemptV2Response {
    private String attemptId;
    private String examId;
    private String doneBy;
    private Double score;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer rating;

}
