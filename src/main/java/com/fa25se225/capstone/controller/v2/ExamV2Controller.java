package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.v2.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.ExamV2Response;
import com.fa25se225.capstone.dto.v2.SubmitAttemptV2Request;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.service.v2.ExamGenerationServiceV2;
import com.fa25se225.capstone.service.v2.ExamGradingServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam-test")
@RequiredArgsConstructor
public class ExamV2Controller {

    private final ExamGenerationServiceV2 generationService;
    private final ExamGradingServiceV2 gradingService;

    @PostMapping("/start/{templateId}")
    public ApiResponse<ExamV2Response> startExam(@PathVariable String templateId) {
        return ApiResponse.success(generationService.startExamFromTemplate(templateId));
    }

    @PostMapping("/submit/{attemptId}")
    public ApiResponse<ExamAttemptV2Response> submitExam(
            @PathVariable String attemptId,
            @RequestBody SubmitAttemptV2Request request) {
        return ApiResponse.success(gradingService.gradeExamAttempt(attemptId, request));
    }

    @PostMapping("/submit2/{attemptId}")
    public ApiResponse<Double> submitExam2(
            @PathVariable String attemptId,
            @RequestBody SubmitAttemptV2Request request) {
        return ApiResponse.success(gradingService.gradeExamAttempt2(attemptId, request));
    }
}