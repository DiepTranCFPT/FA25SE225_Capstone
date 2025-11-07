package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExamAnswerV2Response {

    private String id;
    private String content;

}
