package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.ExamCreationRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.ExamResponse;
import com.fa25se225.capstone.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamController {

    ExamService examService;

    @PostMapping("exam")
    @Operation(summary = "create exam by a subject or many subject")
    public ApiResponse<ExamResponse> createExam(@Valid @RequestBody ExamCreationRequest request) {
        return ApiResponse.success(examService.createExam(request));
    }

    @GetMapping("exam")
    public ApiResponse<ExamResponse> getExamById(@RequestParam String id) {
        return ApiResponse.success(examService.getExamById(id));
    }

    @GetMapping("exams")
    @Operation(summary = "Get all exams")
    public ApiResponse<List<ExamResponse>> getAllExams() {
        return ApiResponse.success(examService.getAllExams());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all exams by user id")
    public ApiResponse<List<ExamResponse>> getAllExamsByUser() {
        return ApiResponse.success(examService.getAllExamsByUser());
    }

    @DeleteMapping("exam/{id}")
    @Operation(summary = "Delete exam by id (soft delete)")
    public ApiResponse<Void> deleteExam(@PathVariable String id) {
        examService.deleteExam(id);
        return ApiResponse.successWithMessage("DELETE SUCCESS");
    }
}
