package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.ExamTemplateUpdateV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {SubjectV2Mapper.class, ExamRuleV2Mapper.class})
public interface ExamTemplateV2Mapper {

    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "createdBy", source = "createdBy.email")
    @Mapping(target = "rules", source = "rules")

    ExamTemplateV2Response toResponse(ExamTemplateV2 template);

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "rules", ignore = true)
    ExamTemplateV2 toEntity(ExamTemplateV2Request request);

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "rules", ignore = true)
    void updateEntity(@MappingTarget ExamTemplateV2 target, ExamTemplateUpdateV2Request source);
}
