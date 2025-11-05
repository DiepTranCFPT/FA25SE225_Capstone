package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.ExamQuestionV2Response;
import com.fa25se225.capstone.entity.v2.ExamQuestionV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {QuestionV2Mapper.class})
public interface ExamQuestionV2Mapper {

    @Mapping(target = "question", source = "question")
    ExamQuestionV2Response toResponse(ExamQuestionV2 examQuestionV2);
}
