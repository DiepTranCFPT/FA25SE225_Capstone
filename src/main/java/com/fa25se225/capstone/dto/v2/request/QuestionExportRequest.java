package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionExportRequest {
    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    @NotEmpty(message = "Question IDs list cannot be empty")
    private List<String> questionIds;
}
