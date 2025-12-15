package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String id;
    private String content;
    private String imgUrl;
    private UserResponse author;

    private String parenCommentId;

    private int replyCount;
    private String replyToUserName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}