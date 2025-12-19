package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionContextRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionContextV2Response;
import com.fa25se225.capstone.entity.v2.QuestionContextV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface QuestionContextMapper {
    QuestionContextV2 toEntity(QuestionContextRequest questionContextRequest);
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    QuestionContextV2Response toResponse(QuestionContextV2 questionContextV2);
    void updateQuestionContext(@MappingTarget QuestionContextV2 questionContextV2, QuestionContextRequest source);
}
