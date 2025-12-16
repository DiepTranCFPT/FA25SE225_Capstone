package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.*;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptDetailResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamV2Response;
import com.fa25se225.capstone.dto.v2.response.SubmitAttemptV2Response;

import java.util.List;

public interface ExamV2Service {

    ExamV2Response startExamFromTemplate(StartSingleExamRequest request);
    SubmitAttemptV2Response gradeExamAttempt(String attemptId, SubmitAttemptV2Request request);
    ExamV2Response startExamFromComboTemplates(StartComboExamRequest request);
    ExamV2Response startRandomExamCombo(StartRandomComboRequest request);
    PageResponse<List<ExamAttemptV2Response>> getMyExamHistory(int pageNo, int pageSize, String... sorts);
    ExamAttemptDetailResponse getAttemptResultDetails(String attemptId);
    void rateAttempt(String attemptId, RateAttemptRequest request);
    void saveExamProgress(String attemptId, SaveProgressRequest request);

    ExamAttemptV2Response manualGradeAttempt(String attemptId, ManualGradeRequest request);

    void requestReview(String attemptId, RequestReviewRequest request);

    PageResponse<List<ExamAttemptV2Response>> getAttemptsForTeacherReview(int pageNo, int pageSize, boolean includePending, boolean includeReviewRequested, String... sorts);

    PageResponse<List<ExamAttemptV2Response>> getAllStudentExamAttempts(int pageNo, int pageSize, String[] sorts);
}
