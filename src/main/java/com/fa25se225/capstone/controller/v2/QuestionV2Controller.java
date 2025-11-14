package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionUpdateV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionManageV2Response;
import com.fa25se225.capstone.dto.v2.response.QuestionV2Response;
import com.fa25se225.capstone.service.v2.QuestionV2Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions-v2")
@RequiredArgsConstructor
@Tag(name = "Question Management")
public class QuestionV2Controller {

    private final QuestionV2Service questionV2Service;

    @PostMapping
    public ApiResponse<QuestionManageV2Response> createQuestion(@Valid @RequestBody QuestionCreationV2Request request) {
        return ApiResponse.success(questionV2Service.createQuestion(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionManageV2Response> updateQuestion(@PathVariable String id, @Valid @RequestBody QuestionUpdateV2Request request) {
        return ApiResponse.success(questionV2Service.updateQuestion(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionManageV2Response> getQuestionById(@PathVariable String id) {
        return ApiResponse.success(questionV2Service.getQuestionById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<List<QuestionManageV2Response>>> getAllQuestions(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts) {
        return ApiResponse.success(questionV2Service.getAllQuestions(pageNo, pageSize, sorts));
    }

    @GetMapping("/subject/{subjectId}")
    public ApiResponse<PageResponse<List<QuestionManageV2Response>>> getQuestionsBySubject(
            @PathVariable String subjectId,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts) {
        return ApiResponse.success(questionV2Service.getQuestionsBySubject(subjectId, pageNo, pageSize, sorts));
    }


    @GetMapping("/topic/{topicId}")
    public ApiResponse<PageResponse<List<QuestionManageV2Response>>> getQuestionsByTopic(
            @PathVariable String topicId,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts) {
        return ApiResponse.success(questionV2Service.getQuestionsByTopic(topicId, pageNo, pageSize, sorts));
    }

    @GetMapping("/created-by/{userId}")
    public ApiResponse<PageResponse<List<QuestionManageV2Response>>> getQuestionsByCreatedBy(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts) {
        return ApiResponse.success(questionV2Service.getQuestionsByCreatedBy(userId, pageNo, pageSize, sorts));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<List<QuestionManageV2Response>>> searchQuestions(@RequestParam String keyword, @RequestParam(defaultValue = "0",
            required = false) int pageNo, @RequestParam(defaultValue = "10", required = false) int pageSize, @RequestParam(required = false) String... sorts) {
        return ApiResponse.success(questionV2Service.searchQuestions(keyword, pageNo, pageSize, sorts));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ApiResponse<String> deleteQuestion(@PathVariable String id) {
        questionV2Service.deleteQuestion(id);
        return ApiResponse.success("Question deleted successfully");
    }
    
}
