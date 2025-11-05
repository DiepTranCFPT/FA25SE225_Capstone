package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.ExamAnswerV2Response;
import com.fa25se225.capstone.dto.v2.ExamQuestionV2Response;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import com.fa25se225.capstone.entity.v2.ExamQuestionV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionV2Mapper.class})
public interface ExamQuestionV2Mapper {

    @Mapping(target = "question", source = "question")
    @Mapping(target = "examQuestionId", source = "id")
    ExamQuestionV2Response toResponse(ExamQuestionV2 examQuestionV2);

}
