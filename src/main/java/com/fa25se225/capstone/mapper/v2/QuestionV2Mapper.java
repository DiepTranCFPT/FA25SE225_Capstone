package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.QuestionDifficultyV2Response;
import com.fa25se225.capstone.dto.v2.QuestionV2Response;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {QuestionDifficultyV2Mapper.class, SubjectV2Mapper.class})
public interface QuestionV2Mapper {
    @Mapping(target = "type", source = "type.value")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "topic", source = "topic.name")
    QuestionV2Response toResponse(QuestionV2 questionV2);
}
