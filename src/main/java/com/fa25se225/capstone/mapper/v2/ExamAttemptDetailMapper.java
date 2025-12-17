package com.fa25se225.capstone.mapper.v2;


import com.fa25se225.capstone.dto.response.APResult;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptDetailResponse;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.utils.APScoreCalculator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = "spring", uses = {ExamQuestionDetailMapper.class, SubjectV2Mapper.class})
public abstract class  ExamAttemptDetailMapper {
    @Autowired
    protected APScoreCalculator apScoreCalculator;

    @Autowired
    protected ExamAttemptV2Repository examAttemptV2Repository;

    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "title", source = "exam.title")
    @Mapping(target = "passingScore", source = "exam.passingScore")
    @Mapping(target = "doneBy", source = "user.email")
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "apResult", expression = "java(mapScaleScore(attempt))")
    public abstract ExamAttemptDetailResponse toResponse(ExamAttemptV2 attempt);

    protected APResult mapScaleScore(ExamAttemptV2 attempt) {
        ExamTemplateV2 template = attempt.getSourceTemplate();
        if (template == null) {
            return new APResult(null, "No template found");
        }
        return apScoreCalculator.calculate(attempt.getScore(), template.getMaxScore(), template.getScoreMapping());
    }
}