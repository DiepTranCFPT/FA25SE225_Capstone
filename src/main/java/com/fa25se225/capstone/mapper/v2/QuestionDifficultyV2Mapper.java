package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionDifficultyV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2CreationRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionTopicV2UpdateRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionDifficultyV2Response;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionDifficultyV2Mapper {
    QuestionDifficultyV2Response toResponse(QuestionDifficultyV2 questionDifficultyV2);
    QuestionDifficultyV2 toEntity(QuestionDifficultyV2Request request);
    void updateEntity(@MappingTarget QuestionDifficultyV2 questionDifficultyV2, QuestionDifficultyV2Request request);
}
