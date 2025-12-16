package com.fa25se225.capstone.dto.request;

import lombok.Builder;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;



@Data
@Builder
public class CommentRequest {

    private String postId;

    private String content;

    private MultipartFile image;

    private String parenCommentId;

}
