package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.response.APResult;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.entity.v2.ExamTemplateV2;
import com.fa25se225.capstone.utils.APScoreCalculator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ExamAttemptV2Mapper {

    @Autowired
    protected APScoreCalculator apScoreCalculator;

    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "title", source = "exam.title")
    @Mapping(target = "doneBy", source = "user.email")
    @Mapping(target = "status", expression = "java(attempt.getStatus() != null ? attempt.getStatus().name() : null)")
    @Mapping(target = "passingScore", expression = "java(attempt.getExam().getPassingScore() != null ? Double.valueOf(attempt.getExam().getPassingScore().doubleValue()) : null)")
    @Mapping(target = "subject", expression = "java(attempt.getExam().getSubject() != null ? attempt.getExam().getSubject().getName() : null)")
    @Mapping(target = "apResult", expression = "java(mapScaleScore(attempt))")
    public abstract ExamAttemptV2Response toResponse(ExamAttemptV2 attempt);

    protected APResult mapScaleScore(ExamAttemptV2 attempt) {
        ExamTemplateV2 template = attempt.getSourceTemplate();
        if (template == null) {
            return new APResult(null, "No template found");
        }
        return apScoreCalculator.calculate(attempt.getScore(), template.getMaxScore(), template.getScoreMapping());
    }
}