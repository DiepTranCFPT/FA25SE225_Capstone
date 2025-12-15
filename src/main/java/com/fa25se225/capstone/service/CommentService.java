package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;

public interface CommentService {
    CommentResponse createComment(CommentRequest request);

    PageResponse<CommentResponse> getCommentsByPostId(String postId, int page, int size);

    PageResponse<CommentResponse> getReplies(String commentId, int page, int size);
}
