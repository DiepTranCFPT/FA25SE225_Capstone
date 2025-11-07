package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamAttemptV2Response {
    private String id;
    private String examId;
    private String doneBy;
    private Double score;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer rating;

}
