package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.CommunityCreationRequest;
import com.fa25se225.capstone.dto.request.CommunityUpdateRequest;
import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.CommunityResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.service.CommunityService;
import com.fa25se225.capstone.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;
    private final CommunityService communityService;

    @PostMapping(value = "/{communityId}/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(@PathVariable String communityId,
                                                @RequestParam("title") @NotBlank String title,
                                                @RequestParam("content") @NotBlank String content,
                                                @RequestParam(value = "image", required = false) MultipartFile image) {

        return ApiResponse.success(postService.createPost(communityId,
                PostCreationRequest.builder()
                        .title(title)
                        .content(content)
                        .image(image)
                        .build()));
    }

    @GetMapping("/{communityId}/posts")
    public ApiResponse<PageResponse<List<PostResponse>>> getPostsByCommunityId(
            @PathVariable String communityId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ApiResponse.success(postService.getPostsByCommunityId(communityId, page, size));
    }

    @GetMapping
    public ApiResponse<List<CommunityResponse>> getAll() {
        return ApiResponse.success(communityService.getAll());
    }

    @GetMapping("/search")
    public ApiResponse<List<CommunityResponse>> searchCommunity(@RequestParam String keyword) {
        return ApiResponse.success(communityService.searchCommunity(keyword));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunityResponse> createCommunity(
                                               @RequestParam(value = "name") @NotBlank String name,
                                               @RequestParam(value = "description") @NotBlank String description,
                                               @RequestParam(value = "image", required = false) MultipartFile image,
                                               @RequestParam(value = "subjectId", required = false) String subjectId) {
        return ApiResponse.success(communityService.createCommunity(CommunityCreationRequest.builder()
                .name(name)
                .description(description)
                .image(image)
                .subjectId(subjectId)
                .build()));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{communityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> updateCommunity(@PathVariable String communityId,
                                               @RequestParam(value = "name") String name,
                                               @RequestParam(value = "description", required = false) String description,
                                               @RequestParam(value = "image", required = false) MultipartFile image) {
        communityService.updateCommunity(communityId, CommunityUpdateRequest.builder()
                                                                            .name(name)
                                                                            .description(description)
                                                                            .image(image).build());
        return ApiResponse.success("Update Community Successfully");
    }
}