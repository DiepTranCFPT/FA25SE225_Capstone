package com.fa25se225.capstone.dto.v2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamV2Response {
    private String id;
    private String title;
    private SubjectV2Response subject;
    private String examAttemptId;
}