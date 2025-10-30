package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;
import com.fa25se225.capstone.entity.LearningMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LearningMaterialMapper {

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "subject", ignore = true)
    LearningMaterial toEntity(LearningMaterialCreationRequest request);

    @Mapping(target = "typeId", source = "type.id")
    @Mapping(target = "typeName", source = "type.name")
    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", expression = "java(entity.getAuthor() != null ? entity.getAuthor().getFirstName() + \" \" + entity.getAuthor().getLastName() : null)")
    LearningMaterialResponse toResponse(LearningMaterial entity);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "subject", ignore = true)
    void updateEntity(@MappingTarget LearningMaterial entity, LearningMaterialUpdateRequest request);
}