package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionExportRequest;

public interface QuestionExportService {
    byte[] exportQuestionsToExcel(QuestionExportRequest request);
}
