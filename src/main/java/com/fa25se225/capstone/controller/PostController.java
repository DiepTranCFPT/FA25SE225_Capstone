package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.service.CommentService;
import com.fa25se225.capstone.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    @PutMapping("/{postId}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> pinPost(@PathVariable String postId) {
        postService.togglePinPost(postId);
        return ApiResponse.success("Post pin status updated");
    }

    @PutMapping("/{postId}")
    public ApiResponse<String> updatePost(@PathVariable String postId, @RequestBody PostUpdateRequest request) {
        postService.updatePost(postId, request);
        return ApiResponse.success("Update Post successfully");
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<PageResponse<CommentResponse>> getCommentsOfPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(commentService.getCommentsByPostId(postId, page, size));
    }
}