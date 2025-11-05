package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamV2Response;
import com.fa25se225.capstone.dto.v2.request.SubmitAttemptV2Request;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.service.v2.ExamV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam-test")
@RequiredArgsConstructor
public class ExamV2Controller {

    private final ExamV2Service examV2Service;

    @PostMapping("/start/{templateId}")
    public ApiResponse<ExamV2Response> startExam(@PathVariable String templateId) {
        return ApiResponse.success(examV2Service.startExamFromTemplate(templateId));
    }

    @PostMapping("/submit/{attemptId}")
    public ApiResponse<ExamAttemptV2Response> submitExam(
            @PathVariable String attemptId,
            @RequestBody SubmitAttemptV2Request request) {
        return ApiResponse.success(examV2Service.gradeExamAttempt(attemptId, request));
    }
}