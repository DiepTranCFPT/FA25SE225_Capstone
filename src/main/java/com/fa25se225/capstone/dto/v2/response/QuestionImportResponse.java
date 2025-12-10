package com.fa25se225.capstone.dto.v2.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportResponse {

    private int totalProcessed;
    private int successCount;
    private int errorCount;
    private List<String> errorMessages;
    private List<String> successQuestionIds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private int rowNumber;
        private String field;
        private String message;
    }
}
