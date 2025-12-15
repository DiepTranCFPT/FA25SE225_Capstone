package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.CommunityResponse;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.entity.forum.Community;
import com.fa25se225.capstone.entity.forum.Post;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityMapper {
    @Mapping(target = "subject", source = "subject.name")
    CommunityResponse toResponse(Community community);
    void updateCommunity(@MappingTarget Community community, PostUpdateRequest request);
}
