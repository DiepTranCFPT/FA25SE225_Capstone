package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.response.PostResponse;

public interface PostService {

    PostResponse createPost(PostCreationRequest request);
    void togglePinPost(String postId);
}
