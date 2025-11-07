package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SubjectV2Mapper.class, ExamRuleV2Mapper.class})
public interface ExamTemplateV2Mapper {

    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "createdBy", source = "createdBy.email")
    @Mapping(target = "rules", source = "rules")
    ExamTemplateV2Response toResponse(ExamTemplateV2 template);
}

