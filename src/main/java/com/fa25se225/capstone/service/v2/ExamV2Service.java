package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.RateAttemptRequest;
import com.fa25se225.capstone.dto.v2.request.StartComboExamRequest;
import com.fa25se225.capstone.dto.v2.request.StartSingleExamRequest;
import com.fa25se225.capstone.dto.v2.request.SubmitAttemptV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptDetailResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamV2Response;
import com.fa25se225.capstone.dto.v2.response.SubmitAttemptV2Response;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExamV2Service {

    ExamV2Response startExamFromTemplate(StartSingleExamRequest request);
    SubmitAttemptV2Response gradeExamAttempt(String attemptId, SubmitAttemptV2Request request);
    ExamV2Response startExamFromComboTemplates(StartComboExamRequest request);
    ExamV2Response startRandomExamCombo(List<String> subjectIds);
    PageResponse<List<ExamAttemptV2Response>> getMyExamHistory(int pageNo, int pageSize, String... sorts);
    ExamAttemptDetailResponse getAttemptResultDetails(String attemptId);
    void rateAttempt(String attemptId, RateAttemptRequest request);
}
