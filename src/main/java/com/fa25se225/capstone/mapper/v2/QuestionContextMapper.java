package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionContextRequest;
import com.fa25se225.capstone.entity.v2.QuestionContextV2;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuestionContextMapper {
    QuestionContextV2 toEntity(QuestionContextRequest questionContextRequest);
}
