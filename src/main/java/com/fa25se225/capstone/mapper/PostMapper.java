package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.PostResponse;
import com.fa25se225.capstone.entity.forum.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class})
public interface PostMapper {
    PostResponse toResponse(Post post);
}
