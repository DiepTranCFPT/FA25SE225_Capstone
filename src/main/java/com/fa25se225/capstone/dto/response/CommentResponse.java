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
    private String authorName;
    private String authorAvatar;
    private LocalDateTime createdAt;

    private int likeCount;
    private boolean isLikedByMe;
    private int replyCount;
    private String replyToUserName;
}