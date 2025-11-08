package com.fa25se225.capstone.dto.v2.response;

import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitAttemptV2Response {
    private String attemptId;
    private AttemptStatusV2 status;
}