package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.ExamResponse;
import com.fa25se225.capstone.entity.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.Context;

import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.ExamQuestionRepository;

@Mapper(componentModel = "spring")
public interface ExamMapper {
    @Mapping(target = "subject", source = "subjectIds", qualifiedByName = "mapSubjectIds")
    @Mapping(target = "questions", source = "questionIds", qualifiedByName = "mapQuestionIds")
    Exam toEntity(com.fa25se225.capstone.dto.request.ExamCreationRequest request, @Context SubjectRepository subjectRepository, @Context ExamQuestionRepository examQuestionRepository);

    @Mapping(target = "subjectNames", source = "subject", qualifiedByName = "mapSubjectNames")
    @Mapping(target = "createdByName", source = "createdBy", qualifiedByName = "mapCreatedByName")
    @Mapping(target = "questionContents", source = "questions", qualifiedByName = "mapQuestionContents")
    ExamResponse toResponse(Exam entity);

    @Named("mapSubjectIds")
    default java.util.List<com.fa25se225.capstone.entity.Subject> mapSubjectIds(java.util.List<String> ids, @Context SubjectRepository subjectRepository) {
        if (ids == null) return null;
        return ids.stream()
            .map(id -> subjectRepository.findById(id).orElse(null))
            .toList();
    }

    @Named("mapQuestionIds")
    default java.util.List<com.fa25se225.capstone.entity.ExamQuestion> mapQuestionIds(java.util.List<String> ids, @Context ExamQuestionRepository examQuestionRepository) {
        if (ids == null) return null;
        return ids.stream()
            .map(id -> examQuestionRepository.findById(id).orElse(null))
            .toList();
    }

    @Named("mapSubjectNames")
    default java.util.List<String> mapSubjectNames(java.util.List<com.fa25se225.capstone.entity.Subject> subjects) {
        if (subjects == null) return null;
        return subjects.stream()
            .map(com.fa25se225.capstone.entity.Subject::getName)
            .toList();
    }

    @Named("mapCreatedByName")
    default String mapCreatedByName(com.fa25se225.capstone.entity.User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("mapQuestionContents")
    default java.util.List<String> mapQuestionContents(java.util.List<com.fa25se225.capstone.entity.ExamQuestion> questions) {
        if (questions == null) return null;
        return questions.stream()
            .map(q -> q.getQuestion() != null ? q.getQuestion().getContent() : null)
            .toList();
    }
}
