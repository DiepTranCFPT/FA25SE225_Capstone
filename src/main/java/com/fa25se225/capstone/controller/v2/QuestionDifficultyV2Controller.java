package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionDifficultyV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionDifficultyV2Response;
import com.fa25se225.capstone.service.v2.QuestionDifficultyV2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-difficulties")
@RequiredArgsConstructor
public class QuestionDifficultyV2Controller {

    private final QuestionDifficultyV2Service questionDifficultyV2Service;

    @PostMapping
    public ApiResponse<QuestionDifficultyV2Response> createDifficulty(@Valid @RequestBody QuestionDifficultyV2Request request) {
        return ApiResponse.success(questionDifficultyV2Service.createDifficulty(request));
    }

    @GetMapping
    public ApiResponse<List<QuestionDifficultyV2Response>> getAllDifficulties() {
        return ApiResponse.success(questionDifficultyV2Service.getAllDifficulties());
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionDifficultyV2Response> updateDifficulty(@PathVariable String id,@RequestBody QuestionDifficultyV2Request request) {
        return ApiResponse.success(questionDifficultyV2Service.updateDifficulty(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteDifficulty(@PathVariable String id) {
        questionDifficultyV2Service.deleteDifficulty(id);
        return ApiResponse.success("Difficulty deleted successfully");
    }
}