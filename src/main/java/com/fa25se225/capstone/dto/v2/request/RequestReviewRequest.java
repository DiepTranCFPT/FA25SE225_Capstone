package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestReviewRequest {
    @NotBlank(message = "Reason must be not empty.")
    private String reason;
}