package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.TeacherProfileRequest;
import com.fa25se225.capstone.dto.response.ParentProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.entity.ParentProfile;
import com.fa25se225.capstone.entity.TeacherProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ParentProfileMapper {
    ParentProfileResponse toResponse(ParentProfile entity);
}

