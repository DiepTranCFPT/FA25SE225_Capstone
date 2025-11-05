package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.v2.request.SubmitAttemptV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamV2Response;

public interface ExamV2Service {

    ExamV2Response startExamFromTemplate(String templateId);
    ExamAttemptV2Response gradeExamAttempt(String attemptId, SubmitAttemptV2Request request);
}
