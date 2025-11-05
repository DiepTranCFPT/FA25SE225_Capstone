package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.SubjectV2Response;
import com.fa25se225.capstone.entity.Subject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubjectV2Mapper {

    SubjectV2Response toResponse(Subject subject);
}
