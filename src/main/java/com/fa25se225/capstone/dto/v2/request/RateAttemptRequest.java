package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RateAttemptRequest {
    @NotNull
    @Min(1) @Max(5)
    private Integer rating;
    private String comment;
}