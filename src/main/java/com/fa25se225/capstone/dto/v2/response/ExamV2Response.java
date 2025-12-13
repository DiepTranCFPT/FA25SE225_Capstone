package com.fa25se225.capstone.dto.v2.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamV2Response {
    private String id;
    private String title;
    private SubjectV2Response subject;
    private String examAttemptId;
    private Integer durationInMinute;
    private Integer passingScore;
    private String belongTo;
    private String attemptSessionToken;
    private List<ExamQuestionV2Response> questions;
}