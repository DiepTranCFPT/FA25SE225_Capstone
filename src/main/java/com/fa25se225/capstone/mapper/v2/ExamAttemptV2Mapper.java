package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamAttemptV2Mapper {
    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "doneBy", source = "user.email")
    ExamAttemptV2Response toResponse(ExamAttemptV2 attempt);
}