package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionImportRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface QuestionImportService {
    QuestionImportResponse importQuestionsFromExcel(MultipartFile file, QuestionImportRequest request);
    byte[] generateExampleTemplate();
}
