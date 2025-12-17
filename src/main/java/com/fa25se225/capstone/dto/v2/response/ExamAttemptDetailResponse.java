package com.fa25se225.capstone.dto.v2.response;

import com.fa25se225.capstone.dto.response.APResult;
import com.fa25se225.capstone.dto.v2.response.SubjectV2Response;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptDetailResponse {
    private String attemptId;
    private String examId;
    private String title;
    private List<SubjectV2Response> subjects;
    private AttemptStatusV2 status;
    private Double score;
    private Integer passingScore;
    private String doneBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer rating;
    private String comment;
    private Boolean isLate;
    private APResult apResult;
    private List<ExamQuestionDetailResponse> questions;
}