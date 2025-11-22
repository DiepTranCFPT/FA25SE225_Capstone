package com.fa25se225.capstone.dto.v2.request;

import lombok.Data;
import java.util.List;

@Data
public class SaveProgressRequest {
    private List<StudentAnswerV2Request> answers;
}