package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class StartRandomComboRequest {
    @NotEmpty(message = "subjectIds are required")
    private List<String> subjectIds;
}