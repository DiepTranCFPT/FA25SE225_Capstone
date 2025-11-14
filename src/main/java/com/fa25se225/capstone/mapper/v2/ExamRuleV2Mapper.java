package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.ExamRuleV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamRuleV2Response;
import com.fa25se225.capstone.entity.v2.ExamRuleV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExamRuleV2Mapper {

    @Mapping(target = "topic", source = "topic.name")
    @Mapping(target = "difficulty", source = "difficulty.name")
    @Mapping(target = "questionType", source = "questionType.value")
    ExamRuleV2Response toResponse(ExamRuleV2 rule);

    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "questionType", ignore = true)
    ExamRuleV2 toEntity(ExamRuleV2Request request);

    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "questionType", ignore = true)
    void updateEntity(@MappingTarget ExamRuleV2 target, ExamRuleV2Request source);
}
