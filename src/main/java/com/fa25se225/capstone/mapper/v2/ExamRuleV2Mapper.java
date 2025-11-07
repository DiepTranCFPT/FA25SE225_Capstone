package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.response.ExamRuleV2Response;
import com.fa25se225.capstone.entity.v2.ExamRuleV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamRuleV2Mapper {

    @Mapping(target = "topic", source = "topic.name")
    @Mapping(target = "difficulty", source = "difficulty.name")
    @Mapping(target = "questionType", source = "questionType.value")
    ExamRuleV2Response toResponse(ExamRuleV2 rule);
}

