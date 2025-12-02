package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.LearningMaterialRatingResponse;
import com.fa25se225.capstone.entity.LearningMaterialRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LearningMaterialRatingMapper {
    
    @Mapping(target = "learningMaterialId", source = "learningMaterial.id")
    @Mapping(target = "learningMaterialTitle", source = "learningMaterial.title")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(getStudentFullName(entity))")
    @Mapping(target = "userId", source = "student.user.id")
    LearningMaterialRatingResponse toResponse(LearningMaterialRating entity);
    
    default String getStudentFullName(LearningMaterialRating rating) {
        if (rating.getStudent() != null && rating.getStudent().getUser() != null) {
            var user = rating.getStudent().getUser();
            return (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                   (user.getLastName() != null ? user.getLastName() : "");
        }
        return null;
    }
}
