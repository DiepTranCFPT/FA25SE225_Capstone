package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequest {
    
    @NotBlank(message = "Lesson ID is required")
    private String lessonId;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}
