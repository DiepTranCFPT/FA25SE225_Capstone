package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.SubjectProgressDto;
import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.ChildOverviewResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.service.ParentProgressService;
import com.fa25se225.capstone.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    private final ParentProgressService parentProgressService;

    @GetMapping("/children/{studentId}/subjects/progress")
    public List<SubjectProgressDto> getChildSubjectProgress(
            @PathVariable String studentId,
            @RequestParam String parentId

    ) {
        return parentProgressService.getChildSubjectProgress(parentId, studentId);
    }

    @GetMapping("/children/{studentId}/subjects/completed")
    public List<SubjectProgressDto> getChildCompletedSubjects(
            @PathVariable String studentId,
            @RequestParam String parentId
    ) {
        return parentProgressService.getChildCompletedSubjects(parentId, studentId);
    }

    @PutMapping("/me")
    public ApiResponse<String> updateProfile(@RequestBody ParentProfileUpdateRequest request) {
        parentService.updateProfile(request);
        return ApiResponse.success("Update successfully");
    }

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