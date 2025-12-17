package com.fa25se225.capstone.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FlashcardSetRequest {
    @NotBlank
    private String title;
    private String description;
    @JsonProperty("isVisible")
    private boolean isPublic;

    @NotEmpty
    private List<FlashcardItemRequest> cards;
}