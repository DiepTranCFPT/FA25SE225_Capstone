package com.fa25se225.capstone.dto.v2.request;

import jakarta.validation.Valid;
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
public class QuestionUpdateV2Request {

    private String content;

    private String type;

    private String difficultyName;

    private String topicName;

    @Valid
    private List<AnswerV2Request> answers;

    private String imageUrl;

    private String audioUrl;

    private String contextId;


}
