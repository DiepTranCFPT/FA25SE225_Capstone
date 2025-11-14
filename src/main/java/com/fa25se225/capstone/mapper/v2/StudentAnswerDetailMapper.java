package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.response.StudentAnswerDetailResponse;
import com.fa25se225.capstone.entity.v2.StudentAnswerV2;
import com.fa25se225.capstone.mapper.v2.AnswerV2Mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AnswerV2Mapper.class})
public interface StudentAnswerDetailMapper {
    @Mapping(target = "studentAnswerId", source = "id")
    @Mapping(target = "selectedAnswerId", source = "selectedAnswer.id")
    @Mapping(target = "correctAnswer", ignore = true)
    StudentAnswerDetailResponse toResponse(StudentAnswerV2 studentAnswer);
}