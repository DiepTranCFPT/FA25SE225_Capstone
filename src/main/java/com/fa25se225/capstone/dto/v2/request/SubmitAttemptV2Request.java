package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SubmitAttemptV2Request {
    private List<StudentAnswerV2Request> answers;
    @NotBlank(message = "Session token is required")
    private String attemptSessionToken;
}