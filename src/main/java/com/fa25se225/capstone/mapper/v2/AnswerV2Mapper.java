package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.AnswerV2Request;
import com.fa25se225.capstone.dto.v2.response.AnswerV2Response;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnswerV2Mapper {

    AnswerV2 toEntity(AnswerV2Request request);

    AnswerV2Response toResponse(AnswerV2 answerV2);

    List<AnswerV2Response> toListResponse(List<AnswerV2> answersV2);
}
