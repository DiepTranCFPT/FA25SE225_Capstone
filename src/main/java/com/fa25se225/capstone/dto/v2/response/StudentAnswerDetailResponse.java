package com.fa25se225.capstone.dto.v2.response;

import com.fa25se225.capstone.dto.v2.response.AnswerV2Response;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentAnswerDetailResponse {
    private String studentAnswerId;
    private String selectedAnswerId;
    private String frqAnswerText;
    private Double score;
    private String feedback;
    private AnswerV2Response correctAnswer;
}