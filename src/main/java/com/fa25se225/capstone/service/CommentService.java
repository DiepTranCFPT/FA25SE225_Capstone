package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(CommentRequest request);

    PageResponse<List<CommentResponse>> getCommentsByPostId(String postId, int page, int size);

    PageResponse<List<CommentResponse>> getReplies(String commentId, int page, int size);

    void deleteComment(String commentId);

    void updateComment(String id, String content);
}
