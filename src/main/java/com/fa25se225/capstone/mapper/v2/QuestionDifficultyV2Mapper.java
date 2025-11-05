package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.QuestionDifficultyV2Response;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuestionDifficultyV2Mapper {
    public QuestionDifficultyV2Response toResponse(QuestionDifficultyV2 questionDifficultyV2);
}
