package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.PostResponse;

import java.util.List;

public interface PostService {

    PostResponse createPost(String communityId, PostCreationRequest request);
    void togglePinPost(String postId);
    void updatePost(String id, PostUpdateRequest request);

    void deletePost(String postId);

    PageResponse<List<PostResponse>> getPostsByCommunityId(String communityId, int page, int size);
    void votePost(String postId, int value);

    PageResponse<List<PostResponse>> getMyPost(int page, int size);
}
