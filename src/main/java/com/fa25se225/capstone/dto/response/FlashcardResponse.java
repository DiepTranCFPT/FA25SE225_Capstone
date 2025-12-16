package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardResponse {
    private String id;
    private String term;
    private String definition;
    private String imageUrl;
    private int displayOrder;
}