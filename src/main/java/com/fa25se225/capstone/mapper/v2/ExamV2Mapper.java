package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.response.ExamV2Response;
import com.fa25se225.capstone.entity.v2.ExamV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SubjectV2Mapper.class, ExamQuestionV2Mapper.class})
public interface ExamV2Mapper {

    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "durationInMinute", source = "duration")
    @Mapping(target = "belongTo", source = "belongTo.id")
    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "examAttemptId", ignore = true)
    ExamV2Response toResponse(ExamV2 examV2);
}
