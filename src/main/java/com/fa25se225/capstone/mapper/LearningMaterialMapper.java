package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;
import com.fa25se225.capstone.entity.LearningMaterial;
// import org.mapstruct.Mapper;
// import org.mapstruct.Mapping;
// import org.mapstruct.MappingTarget;
// import org.mapstruct.NullValuePropertyMappingStrategy;

// @Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LearningMaterialMapper {
    
    LearningMaterial toEntity(LearningMaterialCreationRequest request);
    
    LearningMaterialResponse toResponse(LearningMaterial entity);
    
    void updateEntity(LearningMaterial entity, LearningMaterialUpdateRequest request);
}