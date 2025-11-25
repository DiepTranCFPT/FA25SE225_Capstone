package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.ParentProfileUpdateRequest;
import com.fa25se225.capstone.dto.request.StudentProfileUpdateRequest;
import com.fa25se225.capstone.dto.request.TeacherProfileRequest;
import com.fa25se225.capstone.dto.response.StudentProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.entity.ParentProfile;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.TeacherProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudentProfileMapper {
    StudentProfileResponse toResponse(StudentProfile entity);
    void updateProfile(@MappingTarget StudentProfile profile, StudentProfileUpdateRequest request);
}

