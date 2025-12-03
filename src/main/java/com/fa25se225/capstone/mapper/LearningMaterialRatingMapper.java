package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.LearningMaterialRatingResponse;
import com.fa25se225.capstone.entity.LearningMaterialRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LearningMaterialRatingMapper {
    
    @Mapping(target = "learningMaterialId", source = "learningMaterial.id")
    @Mapping(target = "learningMaterialTitle", source = "learningMaterial.title")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", expression = "java(getUserFullName(entity))")
    LearningMaterialRatingResponse toResponse(LearningMaterialRating entity);
    
    default String getUserFullName(LearningMaterialRating rating) {
        if (rating.getUser() != null) {
            var user = rating.getUser();
            return (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                   (user.getLastName() != null ? user.getLastName() : "");
        }
        return null;
    }
}
