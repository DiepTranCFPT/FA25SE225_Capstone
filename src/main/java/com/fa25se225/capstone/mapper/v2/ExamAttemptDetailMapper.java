package com.fa25se225.capstone.mapper.v2;


import com.fa25se225.capstone.dto.v2.response.ExamAttemptDetailResponse;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {ExamQuestionDetailMapper.class, SubjectV2Mapper.class})
public interface ExamAttemptDetailMapper {
    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "title", source = "exam.title")
    @Mapping(target = "passingScore", source = "exam.passingScore")
    @Mapping(target = "doneBy", source = "user.email")
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "questions", ignore = true)
    ExamAttemptDetailResponse toResponse(ExamAttemptV2 attempt);
}