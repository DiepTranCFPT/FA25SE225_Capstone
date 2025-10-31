package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.QuestionCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.QuestionResponse;
import com.fa25se225.capstone.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "Question Management", description = "APIs for managing questions")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {
    
    private final QuestionService questionService;
    
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new question (Admin only)",
            description = "Creates a new question. The current authenticated user will be set as the creator. Only accessible by administrators.")
    public ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody QuestionCreationRequest request) {
        return ApiResponse.success(questionService.createQuestion(request));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get question by ID (Admin only)",
            description = "Retrieves a question by its ID. Only accessible by administrators.")
    public ApiResponse<QuestionResponse> getQuestionById(@PathVariable String id) {
        return ApiResponse.success(questionService.getQuestionById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get all questions (Admin only)",
            description = "Retrieves a paginated list of all questions. Only accessible by administrators. " +
                    "Example: /questions?pageNo=0&pageSize=10&sorts=createdAt:desc")
    public ApiResponse<PageResponse<List<QuestionResponse>>> getAllQuestions(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of questions per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "createdAt:desc,content:asc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(questionService.getAllQuestions(pageNo, pageSize, sorts));
    }
    
    @GetMapping("/by-teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get questions by teacher (Admin)",
            description = "Retrieves a paginated list of questions created by a specific teacher. Only accessible by administrators. " +
                    "Example: /questions/by-teacher/{teacherId}?pageNo=0&pageSize=10&sorts=createdAt:desc")
    public ApiResponse<PageResponse<List<QuestionResponse>>> getQuestionsByTeacher(
            @Parameter(description = "ID of the teacher who created the questions", required = true)
            @PathVariable String teacherId,
            
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of questions per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "createdAt:desc,content:asc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(questionService.getQuestionsByTeacher(teacherId, pageNo, pageSize, sorts));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update a question (Admin only)",
            description = "Updates an existing question. Only accessible by administrators.")
    public ApiResponse<QuestionResponse> updateQuestion(
            @Parameter(description = "ID of the question to update", required = true)
            @PathVariable String id,
            @Valid @RequestBody QuestionUpdateRequest request
    ) {
        return ApiResponse.success(questionService.updateQuestion(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete a question (Admin only)",
            description = "Soft deletes a question by setting its deleted flag to true. Only accessible by administrators.")
    public ApiResponse<Void> deleteQuestion(
            @Parameter(description = "ID of the question to delete", required = true)
            @PathVariable String id
    ) {
        questionService.deleteQuestion(id);
        return ApiResponse.success(null);
    }
}
