package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.SubjectCreationRequest;
import com.fa25se225.capstone.dto.request.SubjectUpdateRequest;
import com.fa25se225.capstone.dto.response.SubjectResponse;
import com.fa25se225.capstone.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubjectMapper {
    
    Subject toEntity(SubjectCreationRequest request);
    
    SubjectResponse toResponse(Subject entity);
    
    void updateEntity(@MappingTarget Subject entity, SubjectUpdateRequest request);
}
