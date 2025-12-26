package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AIGenerateQuestionRequest {
    @NotBlank
    private String rawText;
    private String topicName;
}
