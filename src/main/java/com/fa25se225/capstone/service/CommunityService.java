package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.CommunityCreationRequest;
import com.fa25se225.capstone.dto.request.CommunityUpdateRequest;
import com.fa25se225.capstone.dto.response.CommunityResponse;

import java.util.List;

public interface CommunityService {
    List<CommunityResponse> getAll();

    void updateCommunity(String communityId, CommunityUpdateRequest request);

    List<CommunityResponse> searchCommunity(String keyword);

    CommunityResponse createCommunity(CommunityCreationRequest request);
}
