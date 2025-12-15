package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {
    @Override
    public CommentResponse createComment(CommentRequest request) {
        return null;
    }

    @Override
    public PageResponse<CommentResponse> getCommentsByPostId(String postId, int page, int size) {
        return null;
    }

    @Override
    public PageResponse<CommentResponse> getReplies(String commentId, int page, int size) {
        return null;
    }
}
