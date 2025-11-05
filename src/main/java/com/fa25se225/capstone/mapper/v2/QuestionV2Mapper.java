package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.ExamAnswerV2Response;
import com.fa25se225.capstone.dto.v2.QuestionV2Response;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionDifficultyV2Mapper.class, SubjectV2Mapper.class, ExamAnswerV2Mapper.class})
public interface QuestionV2Mapper {
    @Mapping(target = "type", source = "type.value")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "topic", source = "topic.name")
    @Mapping(target = "answers", source = "answers", qualifiedByName = "conditionalAnswers")
    QuestionV2Response toResponse(QuestionV2 questionV2);

    List<ExamAnswerV2Response> mapAnswers(List<AnswerV2> answers);

    @Named("conditionalAnswers")
    default List<ExamAnswerV2Response> conditionalAnswers(List<AnswerV2> answers) {
        if (answers == null) {
            return null;
        }
        if (answers.size() == 1 && answers.get(0).getIsCorrect()) {
            return null;
        }
        return mapAnswers(answers);
    }


}
