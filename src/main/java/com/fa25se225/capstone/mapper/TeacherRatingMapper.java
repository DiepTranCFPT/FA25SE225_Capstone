package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.TeacherRatingResponse;
import com.fa25se225.capstone.entity.TeacherRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeacherRatingMapper {
    
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", expression = "java(getTeacherFullName(entity))")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", expression = "java(getUserFullName(entity))")
    @Mapping(target = "learningMaterialId", source = "learningMaterial.id")
    @Mapping(target = "learningMaterialTitle", source = "learningMaterial.title")
    TeacherRatingResponse toResponse(TeacherRating entity);
    
    default String getTeacherFullName(TeacherRating rating) {
        if (rating.getTeacher() != null && rating.getTeacher().getUser() != null) {
            var user = rating.getTeacher().getUser();
            return (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                   (user.getLastName() != null ? user.getLastName() : "");
        }
        return null;
    }
    
    default String getUserFullName(TeacherRating rating) {
        if (rating.getUser() != null) {
            var user = rating.getUser();
            return (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                   (user.getLastName() != null ? user.getLastName() : "");
        }
        return null;
    }
}
