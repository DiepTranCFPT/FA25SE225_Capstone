package com.fa25se225.capstone.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private String id;
    private String userId;
    private String userName;
    private String lessonId;
    private String lessonName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
