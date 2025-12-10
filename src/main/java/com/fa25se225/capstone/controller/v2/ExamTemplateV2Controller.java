package com.fa25se225.capstone.controller.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.ExamRuleV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateUpdateV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamRuleV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateRatingResponse;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.service.v2.ExamTemplateV2Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-templates")
@RequiredArgsConstructor
@Tag(name = "Exam Template Management")
public class ExamTemplateV2Controller {

    private final ExamTemplateV2Service templateService;

    @PostMapping
    public ApiResponse<ExamTemplateV2Response> createTemplate(@Valid @RequestBody ExamTemplateV2Request request) {
        return ApiResponse.success(templateService.createTemplate(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<List<ExamTemplateV2Response>>> getAllTemplates(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(templateService.getAllTemplates(pageNo, pageSize, sorts));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamTemplateV2Response> getById(@PathVariable String id) {
        return ApiResponse.success(templateService.getTemplateById(id));
    }

    @GetMapping("/ratings/{id}")
    public ApiResponse<PageResponse<List<ExamTemplateRatingResponse>>> getByRateId(
            @PathVariable String id,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(defaultValue = "ratingTime:desc") String... sorts) {
        return ApiResponse.success(templateService.getRatingById(id, pageNo, pageSize, sorts));
    }

    @PutMapping("/{id}")
    public ApiResponse<ExamTemplateV2Response> updateTemplate(@PathVariable String id, @RequestBody @Valid ExamTemplateUpdateV2Request request) {
        return ApiResponse.success(templateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success("Delete successfully");
    }

    @PostMapping("/{templateId}/rules")
    public ApiResponse<ExamRuleV2Response> addRule(@PathVariable String templateId, @Valid @RequestBody ExamRuleV2Request request) {
        return ApiResponse.success(templateService.addRule(templateId, request));
    }

    @PutMapping("/rules/{ruleId}")
    public ApiResponse<ExamRuleV2Response> updateRule(@PathVariable String ruleId, @RequestBody ExamRuleV2Request request) {
        return ApiResponse.success(templateService.updateRule(ruleId, request));
    }

    @DeleteMapping("/rules/{ruleId}")
    public ApiResponse<String> deleteRule(@PathVariable String ruleId) {
        templateService.deleteRule(ruleId);
        return ApiResponse.success("Delete successfully");
    }

    @GetMapping("/browse")
    public ApiResponse<PageResponse<List<ExamTemplateV2Response>>> browseTemplates(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String teacherId,
            @RequestParam(defaultValue = "0", required = false) double minRating,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(templateService.browseActiveTemplates(
                subject, teacherId, minRating, pageNo, pageSize, sorts
        ));
    }

    @GetMapping("/my-templates")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<PageResponse<List<ExamTemplateV2Response>>> getMyTemplates(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(templateService.getTemplatesByCurrentUser(pageNo, pageSize, sorts));
    }
}

