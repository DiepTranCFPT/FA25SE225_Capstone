package com.fa25se225.capstone.dto.v2;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamAttemptV2Response {
    String id;
    String examId;
    String doneBy;
    Double score;
    LocalDateTime startAt;
    LocalDateTime endAt;

}
