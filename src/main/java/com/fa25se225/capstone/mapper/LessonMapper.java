package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.LessonCreationRequest;
import com.fa25se225.capstone.dto.request.LessonUpdateRequest;
import com.fa25se225.capstone.dto.response.LessonResponse;
import com.fa25se225.capstone.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonMapper {

    @Mapping(target = "question", ignore = true)
    @Mapping(target = "learningMaterial", ignore = true)
    Lesson toEntity(LessonCreationRequest request);

    @Mapping(target = "questionId", expression = "java(entity.getQuestion() != null ? entity.getQuestion().getId() : null)")
    @Mapping(target = "questionContent", expression = "java(entity.getQuestion() != null ? entity.getQuestion().getContent() : null)")
    @Mapping(target = "learningMaterialId", expression = "java(entity.getLearningMaterial() != null ? entity.getLearningMaterial().getId() : null)")
    @Mapping(target = "learningMaterialTitle", expression = "java(entity.getLearningMaterial() != null ? entity.getLearningMaterial().getTitle() : null)")
    LessonResponse toResponse(Lesson entity);

    @Mapping(target = "question", ignore = true)
    @Mapping(target = "learningMaterial", ignore = true)
    void updateEntity(@MappingTarget Lesson entity, LessonUpdateRequest request);
}
