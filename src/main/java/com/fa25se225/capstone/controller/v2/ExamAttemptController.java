package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.*;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptDetailResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamV2Response;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.v2.response.SubmitAttemptV2Response;
import com.fa25se225.capstone.service.v2.ExamV2Service;
import com.fa25se225.capstone.service.v2.impl.SseNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/exam-attempts")
@RequiredArgsConstructor
@Tag(name = "Exam Attempt Management")
public class ExamAttemptController {

    private final ExamV2Service examV2Service;
    private final SseNotificationService sseService;


    @PostMapping("/start-single")
    public ApiResponse<ExamV2Response> startSingleExam(@Valid @RequestBody StartSingleExamRequest request) {
        return ApiResponse.success(examV2Service.startExamFromTemplate(request));
    }

    @PostMapping("/start-combo")
    public ApiResponse<ExamV2Response> startComboExam(@Valid @RequestBody StartComboExamRequest request) {
        return ApiResponse.success(examV2Service.startExamFromComboTemplates(request));
    }

    @PostMapping("/start-combo-random")
    public ApiResponse<ExamV2Response> startRandomComboExam(@Valid @RequestBody StartRandomComboRequest request) {
        return ApiResponse.success(examV2Service.startRandomExamCombo(request.getSubjectIds()));
    }

    @PostMapping("/{attemptId}/submit")
    public ApiResponse<SubmitAttemptV2Response> submitExam(
            @PathVariable String attemptId,
            @Valid @RequestBody SubmitAttemptV2Request request) {
        return ApiResponse.success(examV2Service.gradeExamAttempt(attemptId, request));
    }

    @GetMapping("/my-history")
    public ApiResponse<PageResponse<List<ExamAttemptV2Response>>> getMyHistory(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(examV2Service.getMyExamHistory(pageNo, pageSize, sorts));
    }

    @GetMapping("/{attemptId}/result")
    public ApiResponse<ExamAttemptDetailResponse> getAttemptResultDetails(@PathVariable String attemptId) {
        return ApiResponse.success(examV2Service.getAttemptResultDetails(attemptId));
    }

    @PostMapping("/{attemptId}/rate")
    public ApiResponse<String> rateAttempt(
            @PathVariable String attemptId,
            @Valid @RequestBody RateAttemptRequest request
    ) {
        examV2Service.rateAttempt(attemptId, request);
        return ApiResponse.success("Rating submitted successfully");
    }

    @GetMapping("/{attemptId}/subscribe")
    @Operation(summary = "Subscribe to grading status updates (SSE)",
            description = "Opens a Server-Sent Event stream to receive notification when grading is complete.")
    public SseEmitter subscribeToAttemptStatus(@PathVariable String attemptId) {
        return sseService.subscribe(attemptId);
    }
}