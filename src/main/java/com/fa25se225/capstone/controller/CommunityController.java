package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;

    @PostMapping(value = "/{communityId}/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(@PathVariable String communityId,
                                                @ModelAttribute @Valid PostCreationRequest request) {
        return ApiResponse.success(postService.createPost(communityId, request));
    }
}