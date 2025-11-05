package com.fa25se225.capstone.dto.v2;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class QuestionDifficultyV2Response {
    private String name;
    private String description;
}
