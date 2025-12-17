package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.FlashcardSetRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.FlashcardSetDetailResponse;
import com.fa25se225.capstone.dto.response.FlashcardSetResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.QuizQuestionResponse;
import com.fa25se225.capstone.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashcard-sets")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    public ApiResponse<FlashcardSetDetailResponse> createSet(@RequestBody @Valid FlashcardSetRequest request) {
        return ApiResponse.success(flashcardService.createFlashcardSet(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlashcardSetDetailResponse> getSetDetail(@PathVariable String id) {
        return ApiResponse.success(flashcardService.getFlashcardSetDetail(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<List<FlashcardSetResponse>>> searchSets(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(flashcardService.searchSets(keyword, page, size));
    }

    @GetMapping("/my-sets")
    public ApiResponse<PageResponse<List<FlashcardSetResponse>>> getMySets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(flashcardService.getMySets(page, size));
    }


    @PutMapping("/{id}")
    public ApiResponse<FlashcardSetDetailResponse> updateSet(@PathVariable String id, @RequestBody @Valid FlashcardSetRequest request) {
        return ApiResponse.success(flashcardService.updateFlashcardSet(id, request));
    }

    @PatchMapping("/{id}/visibility")
    public ApiResponse<String> setVisibility(@PathVariable String id) {
        flashcardService.setVisibility(id);
        return ApiResponse.success("Visibility updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSet(@PathVariable String id) {
        flashcardService.deleteFlashcardSet(id);
        return ApiResponse.success("Delete Flashcard Set successfully");
    }

    @GetMapping("/{id}/quiz")
    public ApiResponse<List<QuizQuestionResponse>> getQuiz(@PathVariable String id) {
        return ApiResponse.success(flashcardService.generateQuiz(id));
    }
}