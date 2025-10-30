package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.QuestionDifficultyCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionDifficultyUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.QuestionDifficultyResponse;
import com.fa25se225.capstone.service.QuestionDifficultyService;
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
@RequestMapping("/question-difficulties")
@RequiredArgsConstructor
@Tag(name = "Question Difficulty Management", description = "APIs for managing question difficulties")
@SecurityRequirement(name = "bearerAuth")
public class QuestionDifficultyController {
    
    private final QuestionDifficultyService questionDifficultyService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new question difficulty (Admin only)",
            description = "Creates a new question difficulty level. Only accessible by administrators.")
    public ApiResponse<QuestionDifficultyResponse> createQuestionDifficulty(@Valid @RequestBody QuestionDifficultyCreationRequest request) {
        return ApiResponse.success(questionDifficultyService.createQuestionDifficulty(request));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get question difficulty by ID (Admin only)",
            description = "Retrieves a question difficulty by its ID. Only accessible by administrators.")
    public ApiResponse<QuestionDifficultyResponse> getQuestionDifficultyById(@PathVariable String id) {
        return ApiResponse.success(questionDifficultyService.getQuestionDifficultyById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all question difficulties (Admin only)",
            description = "Retrieves a paginated list of all question difficulties. Only accessible by administrators. " +
                    "Example: /question-difficulties?pageNo=0&pageSize=10&sorts=name:asc")
    public ApiResponse<PageResponse<List<QuestionDifficultyResponse>>> getAllQuestionDifficulties(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of difficulties per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "name:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(questionDifficultyService.getAllQuestionDifficulties(pageNo, pageSize, sorts));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a question difficulty (Admin only)",
            description = "Updates an existing question difficulty. Only accessible by administrators.")
    public ApiResponse<QuestionDifficultyResponse> updateQuestionDifficulty(
            @Parameter(description = "ID of the question difficulty to update", required = true)
            @PathVariable String id,
            @Valid @RequestBody QuestionDifficultyUpdateRequest request
    ) {
        return ApiResponse.success(questionDifficultyService.updateQuestionDifficulty(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a question difficulty (Admin only)",
            description = "Soft deletes a question difficulty by setting its deleted flag to true. Only accessible by administrators.")
    public ApiResponse<Void> deleteQuestionDifficulty(
            @Parameter(description = "ID of the question difficulty to delete", required = true)
            @PathVariable String id
    ) {
        questionDifficultyService.deleteQuestionDifficulty(id);
        return ApiResponse.success(null);
    }
}
