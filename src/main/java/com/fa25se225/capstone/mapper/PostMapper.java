package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.PostCreationRequest;
import com.fa25se225.capstone.dto.request.PostUpdateRequest;
import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.entity.forum.Post;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class})
public interface PostMapper {
    PostResponse toResponse(Post post);
    void updatePost(@MappingTarget Post post, PostUpdateRequest request);
}
