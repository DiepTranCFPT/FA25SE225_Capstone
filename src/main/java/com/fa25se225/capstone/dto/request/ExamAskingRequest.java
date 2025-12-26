package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExamAskingRequest {
    @NotEmpty(message = "Attempt Id is require")
    private String attemptId;

    @NotEmpty(message = "Question content is require")
    private String questionContent;

    private List<String> answerContents;

    @NotEmpty(message = "Student answer is require")
    private String studentAnswer;

    @NotEmpty(message = "Student asking is require")
    private String studentAsking;

    private String questionContext;

    @NotEmpty(message = "Done By is require")
    private String doneBy;
}
