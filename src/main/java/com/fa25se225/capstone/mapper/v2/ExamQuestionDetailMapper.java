package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.response.ExamQuestionDetailResponse;
import com.fa25se225.capstone.entity.v2.ExamQuestionV2;
import com.fa25se225.capstone.mapper.v2.QuestionV2Mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {QuestionV2Mapper.class, StudentAnswerDetailMapper.class})
public interface ExamQuestionDetailMapper {
    @Mapping(target = "examQuestionId", source = "id")
    @Mapping(target = "question", source = "question")
    @Mapping(target = "studentAnswer", ignore = true)
    ExamQuestionDetailResponse toResponse(ExamQuestionV2 examQuestion);
}