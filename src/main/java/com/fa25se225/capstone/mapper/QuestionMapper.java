package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.QuestionCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionUpdateRequest;
import com.fa25se225.capstone.dto.response.QuestionResponse;
import com.fa25se225.capstone.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionMapper {
    
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Question toEntity(QuestionCreationRequest request);
    
    @Mapping(target = "difficultyId", source = "difficulty.id")
    @Mapping(target = "difficultyName", source = "difficulty.name")
    @Mapping(target = "createdById", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)")
    @Mapping(target = "createdByName", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getFirstName() + \" \" + entity.getCreatedBy().getLastName() : null)")
    QuestionResponse toResponse(Question entity);
    
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget Question entity, QuestionUpdateRequest request);
}
