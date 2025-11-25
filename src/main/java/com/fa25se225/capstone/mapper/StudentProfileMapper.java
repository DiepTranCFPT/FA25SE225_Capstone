package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.TeacherProfileRequest;
import com.fa25se225.capstone.dto.response.StudentProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.TeacherProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentProfileMapper {
    StudentProfileResponse toResponse(StudentProfile entity);
}

