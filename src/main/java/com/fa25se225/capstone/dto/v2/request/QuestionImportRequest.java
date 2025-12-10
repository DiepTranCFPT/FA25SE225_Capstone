package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportRequest {

    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    private boolean skipErrors = false; // If true, skip invalid rows and continue processing
}
