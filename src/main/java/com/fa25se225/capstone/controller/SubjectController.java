package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.SubjectCreationRequest;
import com.fa25se225.capstone.dto.request.SubjectUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.SubjectResponse;
import com.fa25se225.capstone.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Tag(name = "Subject Management", description = "APIs for managing subjects")
public class SubjectController {
    
    private final SubjectService subjectService;
    
    @PostMapping
    @Operation(summary = "Create a new subject",
            description = "Creates a new subject.")
    public ApiResponse<SubjectResponse> createSubject(@Valid @RequestBody SubjectCreationRequest request) {
        return ApiResponse.success(subjectService.createSubject(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID",
            description = "Retrieves a subject by its ID.")
    public ApiResponse<SubjectResponse> getSubjectById(@PathVariable String id) {
        return ApiResponse.success(subjectService.getSubjectById(id));
    }

    @GetMapping
    @Operation(summary = "Get all subjects",
            description = "Retrieves a paginated list of all subjects. " +
                    "Example: /subjects?pageNo=0&pageSize=10&sorts=name:asc")
    public ApiResponse<PageResponse<List<SubjectResponse>>> getAllSubjects(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,

            @Parameter(description = "Number of subjects per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,

            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "name:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(subjectService.getAllSubjects(pageNo, pageSize, sorts));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a subject",
            description = "Updates an existing subject.")
    public ApiResponse<SubjectResponse> updateSubject(
            @Parameter(description = "ID of the subject to update", required = true)
            @PathVariable String id,
            @Valid @RequestBody SubjectUpdateRequest request
    ) {
        return ApiResponse.success(subjectService.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subject",
            description = "Soft deletes a subject by setting its deleted flag to true.")
    public ApiResponse<Void> deleteSubject(
            @Parameter(description = "ID of the subject to delete", required = true)
            @PathVariable String id
    ) {
        subjectService.deleteSubject(id);
        return ApiResponse.success(null);
    }
}
