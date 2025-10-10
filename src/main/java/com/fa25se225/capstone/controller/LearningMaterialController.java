package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;
import com.fa25se225.capstone.service.LearningMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning-materials")
@RequiredArgsConstructor
@Tag(name = "Learning Material Management", description = "APIs for managing learning materials")
public class LearningMaterialController {
    
    private final LearningMaterialService learningMaterialService;
    
    @PostMapping
    @Operation(summary = "Create a new learning material",
            description = "Creates a new learning material. The current authenticated user will be set as the author.")
    public ApiResponse<LearningMaterialResponse> create(@Valid @RequestBody LearningMaterialCreationRequest request) {
        return ApiResponse.success(learningMaterialService.create(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get learning material by ID",
            description = "Retrieves a learning material by its ID. Only public materials or materials owned by the current user can be accessed.")
    public ApiResponse<LearningMaterialResponse> getById(@PathVariable String id) {
        return ApiResponse.success(learningMaterialService.getById(id));
    }
    
    @GetMapping
    @Operation(summary = "Get all learning materials with pagination and sorting (Admin)",
            description = "Retrieves a paginated list of all learning materials. This endpoint is typically for admin use. " +
                    "Example: /learning-materials?pageNo=0&pageSize=10&sorts=title:asc&sorts=createdAt:desc")
    public ApiResponse<PageResponse<List<LearningMaterialResponse>>> getAll(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of materials per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "title:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(learningMaterialService.getAll(pageNo, pageSize, sorts));
    }
    
    @GetMapping("/my-materials")
    @Operation(summary = "Get current user's learning materials",
            description = "Retrieves a paginated list of learning materials created by the current authenticated user.")
    public ApiResponse<PageResponse<List<LearningMaterialResponse>>> getMyMaterials(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of materials per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "title:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(learningMaterialService.getMyMaterials(pageNo, pageSize, sorts));
    }
    
    @GetMapping("/public")
    @Operation(summary = "Get public learning materials",
            description = "Retrieves a paginated list of public learning materials that are accessible to all users.")
    public ApiResponse<PageResponse<List<LearningMaterialResponse>>> getPublicMaterials(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of materials per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "title:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(learningMaterialService.getPublicMaterials(pageNo, pageSize, sorts));
    }
    
    @GetMapping("/by-subject/{subjectId}")
    @Operation(summary = "Get learning materials by subject",
            description = "Retrieves a paginated list of learning materials filtered by subject ID.")
    public ApiResponse<PageResponse<List<LearningMaterialResponse>>> getBySubject(
            @PathVariable String subjectId,
            
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of materials per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "title:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(learningMaterialService.getBySubject(subjectId, pageNo, pageSize, sorts));
    }
    
    @GetMapping("/by-type/{typeId}")
    @Operation(summary = "Get learning materials by type",
            description = "Retrieves a paginated list of learning materials filtered by material type ID.")
    public ApiResponse<PageResponse<List<LearningMaterialResponse>>> getByType(
            @PathVariable String typeId,
            
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of materials per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "title:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(learningMaterialService.getByType(typeId, pageNo, pageSize, sorts));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search learning materials by keyword",
            description = "Searches learning materials by keyword in title and description.")
    public ApiResponse<PageResponse<List<LearningMaterialResponse>>> searchByKeyword(
            @Parameter(description = "Search keyword", example = "java programming", required = true)
            @RequestParam String keyword,
            
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            
            @Parameter(description = "Number of materials per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            
            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "title:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(learningMaterialService.searchByKeyword(keyword, pageNo, pageSize, sorts));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update learning material",
            description = "Updates a learning material. Only the author can update their own materials.")
    public ApiResponse<LearningMaterialResponse> update(
            @PathVariable String id,
            @Valid @RequestBody LearningMaterialUpdateRequest request
    ) {
        return ApiResponse.success(learningMaterialService.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete learning material",
            description = "Performs a soft delete on a learning material. Only the author can delete their own materials.")
    public ApiResponse<String> delete(@PathVariable String id) {
        learningMaterialService.delete(id);
        return ApiResponse.success("Learning material deleted successfully");
    }
}