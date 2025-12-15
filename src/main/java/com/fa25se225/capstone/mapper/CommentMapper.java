package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.CommentResponse;
import com.fa25se225.capstone.entity.forum.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "parenCommentId", source = "parent.id")
    @Mapping(target = "replyToUserName", expression = "java(getReplyToUserName(comment))")
    CommentResponse toResponse(Comment comment);

    default String getReplyToUserName(Comment comment) {
        if (comment.getReplyToUser() != null) {
            return comment.getReplyToUser().getFirstName() + " " + comment.getReplyToUser().getLastName();
        }
        return null;
    }
}