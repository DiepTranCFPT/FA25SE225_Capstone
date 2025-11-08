package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.ExamTemplateUpdateV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2CreationRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2UpdateRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionTopicV2Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "creatAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    QuestionTopicV2 toEntity(QuestionTopicV2CreationRequest request);

    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "createdBy", source = "createdBy.email")
    QuestionTopicV2Response toResponse(QuestionTopicV2 entity);

    void updateEntity(@MappingTarget QuestionTopicV2 target, QuestionTopicV2UpdateRequest source);
}
