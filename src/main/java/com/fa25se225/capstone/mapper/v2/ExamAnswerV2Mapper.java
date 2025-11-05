package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.ExamAnswerV2Response;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamAnswerV2Mapper {
    ExamAnswerV2Response toResponse(AnswerV2 answerV2);

    List<ExamAnswerV2Response> toListResponse(List<AnswerV2> answerV2s);
}
