package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.QuestionDifficultyCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionDifficultyUpdateRequest;
import com.fa25se225.capstone.dto.response.QuestionDifficultyResponse;
import com.fa25se225.capstone.entity.QuestionDifficulty;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionDifficultyMapper {
    
    QuestionDifficulty toEntity(QuestionDifficultyCreationRequest request);
    
    QuestionDifficultyResponse toResponse(QuestionDifficulty entity);
    
    void updateEntity(@MappingTarget QuestionDifficulty entity, QuestionDifficultyUpdateRequest request);
}
