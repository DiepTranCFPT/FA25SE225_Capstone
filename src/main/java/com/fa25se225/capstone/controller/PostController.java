package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.CommentRequest;
import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.service.CommentService;
import com.fa25se225.capstone.service.PostService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/my-posts")
    public ApiResponse<PageResponse<List<PostResponse>>>getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(postService.getMyPost(page, size));
    }

    @PutMapping("/{postId}")
    public ApiResponse<String> updatePost(@PathVariable String postId, @RequestBody PostUpdateRequest request) {
        postService.updatePost(postId, request);
        return ApiResponse.success("Update Post successfully");
    }

    @PostMapping(value = "/{postId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommentResponse> createComment(@PathVariable String postId,
                                                      @RequestParam(value = "content", required = false) String content,
                                                      @RequestParam(value = "parenCommentId", required = false) String parenCommentId,
                                                      @RequestParam(value = "image", required = false) MultipartFile image) {
        return ApiResponse.success(commentService.createComment(CommentRequest.builder()
                        .postId(postId)
                        .content(content)
                        .image(image)
                        .parenCommentId(parenCommentId)
                .build()));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<String> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ApiResponse.success("Delete Post successfully");
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<PageResponse<List<CommentResponse>>> getCommentsOfPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(commentService.getCommentsByPostId(postId, page, size));
    }

    @PostMapping("/{postId}/vote")
    public ApiResponse<Void> votePost(@PathVariable String postId, @RequestParam int value) {
        postService.votePost(postId, value);
        return ApiResponse.success(null);
    }
}