package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.LessonCreationRequest;
import com.fa25se225.capstone.dto.request.LessonUpdateRequest;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.LessonResponse;
import com.fa25se225.capstone.dto.response.LessonProgressResponse;
import com.fa25se225.capstone.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson Management", description = "APIs for managing lessons")
public class LessonController {

    private final LessonService lessonService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> create(
            @Valid @ModelAttribute LessonCreationRequest request,
            @RequestParam MultipartFile file,
            @RequestParam MultipartFile video
    ) {
        return ApiResponse.success(lessonService.create(request,file,video));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID", description = "Retrieves a lesson by its unique identifier")
    public ApiResponse<LessonResponse> getById(@PathVariable String id) {
        return ApiResponse.success(lessonService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all lessons with pagination", description = "Retrieves all lessons with pagination and sorting options")
    public ApiResponse<PageResponse<List<LessonResponse>>> getAll(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(lessonService.getAll(pageNo, pageSize, sorts));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lesson", description = "Updates an existing lesson with the provided information")
    public ApiResponse<LessonResponse> update(
            @PathVariable String id,
            @Valid @RequestBody LessonUpdateRequest request
    ) {
        return ApiResponse.success(lessonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lesson", description = "Soft deletes a lesson by setting its deleted flag to true")
    public ApiResponse<String> delete(@PathVariable String id) {
        lessonService.delete(id);
        return ApiResponse.success("Lesson deleted successfully");
    }

    @GetMapping("/by-learning-material/{learningMaterialId}")
    @Operation(summary = "Get lessons by learning material",
            description = "Retrieves a paginated list of lessons filtered by learning material ID")
    public ApiResponse<PageResponse<List<LessonResponse>>> getLessonsByLearningMaterial(
            @PathVariable String learningMaterialId,

            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,

            @Parameter(description = "Number of lessons per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,

            @Parameter(description = "Sorting criteria. Format: `fieldName:direction`. Multiple criteria can be provided.",
                    example = "name:asc,createdAt:desc")
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(lessonService.getLessonsByLearningMaterial(learningMaterialId, pageNo, pageSize, sorts));
    }

    @PutMapping("/{lessonId}/progress")
    @Operation(summary = "Save lesson video progress", description = "Save the last watched second for the current user in a lesson video")
    public ApiResponse<String> saveLessonVideoProgress(
            @PathVariable String lessonId,
            @RequestParam int lastWatchedSecond,
            @RequestParam boolean completed
    ) {
        lessonService.saveLessonVideoProgress(lessonId, lastWatchedSecond,completed);
        return ApiResponse.success("Progress saved");
    }
    @GetMapping("/progress/by-learning-material/{learningMaterialId}")
    @Operation(summary = "Get all lessons with user progress by learning material", description = "Retrieves all lessons for the current user with progress and next-to-continue flag, filtered by learning material")
    public ApiResponse<List<LessonProgressResponse>> getLessonsWithProgressByLearningMaterial(@PathVariable String learningMaterialId) {
        return ApiResponse.success(lessonService.getLessonsWithProgressByLearningMaterial(learningMaterialId));
    }
}
