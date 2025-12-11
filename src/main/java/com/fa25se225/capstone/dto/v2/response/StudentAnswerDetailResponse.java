package com.fa25se225.capstone.dto.v2.response;

import com.fa25se225.capstone.dto.v2.response.AnswerV2Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerDetailResponse {
    private String studentAnswerId;
    private String selectedAnswerId;
    private String frqAnswerText;
    private Double score;
    private String feedback;
    private AnswerV2Response correctAnswer;
}