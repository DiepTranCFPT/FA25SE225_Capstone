package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamRuleV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamRuleV2Response;

import java.util.List;

public interface ExamTemplateV2Service {
    ExamTemplateV2Response createTemplate(ExamTemplateV2Request request);
    ExamTemplateV2Response updateTemplate(String id, ExamTemplateV2Request request);
    void deleteTemplate(String id);
    ExamTemplateV2Response getTemplateById(String id);
    PageResponse<List<ExamTemplateV2Response>> getAllTemplates(int pageNo, int pageSize, String... sorts);

    ExamRuleV2Response addRule(String templateId, ExamRuleV2Request request);
    ExamRuleV2Response updateRule(String ruleId, ExamRuleV2Request request);
    void deleteRule(String ruleId);
}

