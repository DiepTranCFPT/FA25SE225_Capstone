package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(@ModelAttribute @Valid PostCreationRequest request) {
        return ApiResponse.success(postService.createPost(request));
    }

    @PutMapping("/posts/{postId}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> pinPost(@PathVariable String postId) {
        postService.togglePinPost(postId);
        return ApiResponse.success("Post pin status updated");
    }

}