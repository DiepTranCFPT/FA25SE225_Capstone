
package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionUpdateV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamAnswerV2Response;
import com.fa25se225.capstone.dto.v2.response.QuestionManageV2Response;
import com.fa25se225.capstone.dto.v2.response.QuestionV2Response;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {QuestionDifficultyV2Mapper.class, SubjectV2Mapper.class, ExamAnswerV2Mapper.class, AnswerV2Mapper.class})
public interface QuestionV2Mapper {
    @Mapping(target = "type", source = "type.value")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "topic", source = "topic.name")
//    @Mapping(target = "answers", source = "answers", qualifiedByName = "conditionalAnswers")
    @Mapping(target = "answers", source = "answers")

    QuestionV2Response toResponse(QuestionV2 questionV2);


//    List<ExamAnswerV2Response> mapAnswers(List<AnswerV2> answers);
//
//    @Named("conditionalAnswers")
//    default List<ExamAnswerV2Response> conditionalAnswers(List<AnswerV2> answers) {
//        if (answers == null) {
//            return null;
//        }
//        if (answers.size() == 1 && answers.get(0).getIsCorrect()) {
//            return null;
//        }
//        return mapAnswers(answers);
//    }

    @Mapping(target = "type", expression = "java(QuestionType.valueOf(request.getType().toUpperCase()))")
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "answers", source = "answers")
    QuestionV2 toEntity(QuestionCreationV2Request request);



}