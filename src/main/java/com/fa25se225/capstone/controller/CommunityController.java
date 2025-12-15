package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.CommunityUpdateRequest;
import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.CommunityResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.service.CommunityService;
import com.fa25se225.capstone.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;
    private final CommunityService communityService;

    @PostMapping(value = "/{communityId}/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(@PathVariable String communityId,
                                                @ModelAttribute @Valid PostCreationRequest request) {
        return ApiResponse.success(postService.createPost(communityId, request));
    }

    @GetMapping
    public ApiResponse<List<CommunityResponse>> getAll() {
        return ApiResponse.success(communityService.getAll());
    }

    @PutMapping("/{communityId}")
    public ApiResponse<String> updateCommunity(@PathVariable String communityId, @ModelAttribute CommunityUpdateRequest request) {
        communityService.updateCommunity(communityId, request);

        return ApiResponse.success("Update Community Successfully");
    }
}