package com.fa25se225.capstone.dto.request;

import lombok.Data;

import org.springframework.web.multipart.MultipartFile;



@Data
public class CommentRequest {

    private String postId;

    private String content;

    private MultipartFile image;

    private String parenCommentId;

}
