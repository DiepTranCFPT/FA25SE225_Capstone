package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreationV2Request {

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Question type is required")
    private String type;

    @NotNull(message = "Subject name is required")
    private String subject;

    @NotBlank(message = "Difficulty name is required")
    private String difficultyName;

    @NotBlank(message = "Topic name is required")
    private String topicName;

    private List<AnswerV2Request> answers;


}
