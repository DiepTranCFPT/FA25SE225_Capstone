package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.ExamTemplateUpdateV2Request;
import com.fa25se225.capstone.dto.v2.request.ExamTemplateV2Request;
import com.fa25se225.capstone.dto.v2.request.ScoreRange;
import com.fa25se225.capstone.dto.v2.response.ExamTemplateV2Response;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {SubjectV2Mapper.class, ExamRuleV2Mapper.class})
public abstract class ExamTemplateV2Mapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "createdBy", source = "createdBy.email")
    @Mapping(target = "rules", source = "rules")
    @Mapping(target = "scoreMapping", source = "scoreMapping", qualifiedByName = "stringToMap")
    public abstract ExamTemplateV2Response toResponse(ExamTemplateV2 template);

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "rules", ignore = true)
    @Mapping(target = "scoreMapping", source = "scoreMapping", qualifiedByName = "mapToString")
    public abstract ExamTemplateV2 toEntity(ExamTemplateV2Request request);

    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "rules", ignore = true)
    @Mapping(target = "scoreMapping", source = "scoreMapping", qualifiedByName = "mapToString")
    public abstract void updateEntity(@MappingTarget ExamTemplateV2 target, ExamTemplateUpdateV2Request source);

    @Named("mapToString")
    public String mapToString(Map<String, ScoreRange> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INVALID_SCORE_RANGE);
        }
    }

    @Named("stringToMap")
    public Map<String, ScoreRange> stringToMap(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, ScoreRange>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}