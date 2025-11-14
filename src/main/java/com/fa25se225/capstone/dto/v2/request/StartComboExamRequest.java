package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class StartComboExamRequest {
    @NotEmpty(message = "templateIds are required")
    private List<String> templateIds;
}