package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FlashcardItemRequest {
    @NotBlank
    private String term;
    @NotBlank
    private String definition;
    private String imageUrl;
}