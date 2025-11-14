package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartSingleExamRequest {
    @NotBlank(message = "templateId is required")
    private String templateId;
}