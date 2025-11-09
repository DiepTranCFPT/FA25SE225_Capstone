package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    @NotBlank(message = "Difficulty name is required")
    private String difficultyName;

    @NotBlank(message = "Topic name is required")
    private String topicName;

    @NotEmpty
    @Valid
    private List<AnswerV2Request> answers;


}
