package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2CreationRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2UpdateRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import com.fa25se225.capstone.service.v2.QuestionTopicV2Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-topics")
@RequiredArgsConstructor
@Tag(name = "Question Topic Management")
public class QuestionTopicV2Controller {
    private final QuestionTopicV2Service questionTopicV2Service;


    @GetMapping
    public ApiResponse<PageResponse<List<QuestionTopicV2Response>>> getAllTopics(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(questionTopicV2Service.getAllTopics(pageNo, pageSize, sorts));
    }


    @PostMapping
    public ApiResponse<QuestionTopicV2Response> createTopic(@Valid @RequestBody QuestionTopicV2CreationRequest request) {
        return ApiResponse.success(questionTopicV2Service.createTopic(request));
    }

    @PutMapping("/{topicId}")
    public ApiResponse<QuestionTopicV2Response> updateTopic(@PathVariable String topicId, @Valid @RequestBody QuestionTopicV2UpdateRequest request) {
        return ApiResponse.success(questionTopicV2Service.updateTopic(topicId, request));
    }

    @DeleteMapping("/{topicId}")
    public ApiResponse<String> deleteTopic(@PathVariable String topicId) {
        questionTopicV2Service.deleteTopic(topicId);
        return ApiResponse.success("Topic deleted successfully");
    }


    @GetMapping("/by-subject/{subjectId}")
    public ApiResponse<List<QuestionTopicV2Response>> getTopicsBySubject(@PathVariable String subjectId) {
        return ApiResponse.success(questionTopicV2Service.getTopicsBySubject(subjectId));
    }

    @GetMapping("/my-topics")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<QuestionTopicV2Response>> getMyTopics() {
        return ApiResponse.success(questionTopicV2Service.getTopicsByCurrentUser());
    }




}
