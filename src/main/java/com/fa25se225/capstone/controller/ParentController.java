package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.LinkStudentRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.UnlinkStudentRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.ChildOverviewResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/link-student")
    public ApiResponse<String> linkStudent(@Valid @RequestBody LinkStudentRequest request) {
        parentService.linkStudent(request);
        return ApiResponse.success("Student linked successfully");
    }

    @PostMapping("/unlink-student")
    public ApiResponse<String> unlinkStudent(@Valid @RequestBody UnlinkStudentRequest request) {
        parentService.unlinkStudent(request);
        return ApiResponse.success("Student unlinked successfully");
    }

    @GetMapping("/children")
    public ApiResponse<List<ChildOverviewResponse>> getChildren() {
        return ApiResponse.success(parentService.getChildrenOverview());
    }

    @GetMapping("/children/{studentId}/exam-history")
    public ApiResponse<PageResponse<List<ExamAttemptV2Response>>> getChildExamHistory(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(parentService.getChildExamHistory(studentId, pageNo, pageSize, sorts));
    }
}