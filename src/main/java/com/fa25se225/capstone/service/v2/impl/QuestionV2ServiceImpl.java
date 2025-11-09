package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.AnswerV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionUpdateV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionV2Response;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.AnswerV2Mapper;
import com.fa25se225.capstone.mapper.v2.QuestionV2Mapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.v2.QuestionV2Service;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionV2ServiceImpl implements QuestionV2Service {

    private final QuestionV2Repository questionV2Repository;
    private final AccountUtil accountUtil;
    private final SubjectRepository subjectRepository;
    private final QuestionDifficultyV2Repository questionDifficultyV2Repository;
    private final QuestionTopicV2Repository questionTopicV2Repository;
    private final UserRepository userRepository;
    private final QuestionV2Mapper questionV2Mapper;
    private final AnswerV2Mapper answerV2Mapper;
    private final PageHelper pageHelper;

    @Override
    @Transactional
    public QuestionV2Response createQuestion(QuestionCreationV2Request request) {
        log.info("Creating new question V2");

        validateQuestionAnswersRequest(request.getAnswers(), request.getType());

        User currentUser = accountUtil.getCurrentUser();

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));

        QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));

        QuestionV2 questionV2 = questionV2Mapper.toEntity(request);
        questionV2.setCreatedBy(currentUser);
        questionV2.setSubject(subject);
        questionV2.setDifficulty(difficulty);
        questionV2.setTopic(topic);

        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            List<AnswerV2> answers = request.getAnswers().stream()
                    .map(answerRequest -> {
                        AnswerV2 answer = answerV2Mapper.toEntity(answerRequest);
                        answer.setQuestion(questionV2);
                        return answer;
                    })
                    .collect(Collectors.toList());
            questionV2.setAnswers(answers);
        }

        QuestionV2 savedQuestion = questionV2Repository.save(questionV2);
        log.info("Question V2 created successfully with ID: {}", savedQuestion.getId());

        return questionV2Mapper.toResponse(savedQuestion);
    }

//    private Subject getSubjectByNameOrElseCreateTheNewOne(String name){
//        return subjectRepository.findByNameIgnoreCase(name)
//                .orElse(subjectRepository.save(Subject.builder().name(name).build()));
//    }
//
//    private QuestionTopicV2 getQuestionTopicByNameOrElseCreateTheNewOne(String name){
//        return questionTopicV2Repository.findByNameIgnoreCase(name)
//                .orElse(questionTopicV2Repository.save(QuestionTopicV2.builder().name(name).build()));
//    }


    @Override
    @Transactional
    public QuestionV2Response updateQuestion(String id, QuestionUpdateV2Request request) {
        log.info("Updating question V2 with ID: {}", id);


        QuestionV2 existingQuestion = questionV2Repository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_V2_NOT_FOUND));

        // Validate references
        QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));

        QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));

        // Update question properties
        if(StringUtils.hasText(request.getContent())) {
            existingQuestion.setContent(request.getContent());
        }
        try{
            if(StringUtils.hasText(request.getType())) {
                existingQuestion.setType(QuestionType.fromValue(request.getType().toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
        }


        existingQuestion.setDifficulty(difficulty);
        existingQuestion.setTopic(topic);

        if(Objects.nonNull(request.getAnswers())) {
            validateQuestionAnswersRequest(request.getAnswers(), request.getType());

            existingQuestion.getAnswers().clear();
            if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
                List<AnswerV2> answers = request.getAnswers().stream()
                        .map(answerRequest -> {
                            AnswerV2 answer = answerV2Mapper.toEntity(answerRequest);
                            answer.setQuestion(existingQuestion);
                            return answer;
                        })
                        .toList();
                existingQuestion.getAnswers().addAll(answers);
            }
        }

        QuestionV2 updatedQuestion = questionV2Repository.save(existingQuestion);
        log.info("Question V2 updated successfully with ID: {}", updatedQuestion.getId());

        return questionV2Mapper.toResponse(updatedQuestion);
    }

    @Override
    public QuestionV2Response getQuestionById(String id) {
        log.info("Getting question V2 by ID: {}", id);

        QuestionV2 questionV2 = questionV2Repository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_V2_NOT_FOUND));

        return questionV2Mapper.toResponse(questionV2);
    }

    @Override
    public PageResponse<List<QuestionV2Response>> getAllQuestions(int pageNo, int pageSize, String... sorts) {
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findAll(pageable);

        List<QuestionV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toResponse)
                .collect(Collectors.toList());


        return PageResponse.<List<QuestionV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    public PageResponse<List<QuestionV2Response>> getQuestionsBySubject(String subjectId, int pageNo, int pageSize, String... sorts) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new AppException(ErrorCode.SUBJECT_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findBySubjectId(subjectId, pageable);

        List<QuestionV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<List<QuestionV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }



    @Override
    public PageResponse<List<QuestionV2Response>> getQuestionsByTopic(String topicId, int pageNo, int pageSize, String... sorts) {
        if (!questionTopicV2Repository.existsById(topicId)) {
            throw new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findByTopicId(topicId, pageable);

        List<QuestionV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<List<QuestionV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    public PageResponse<List<QuestionV2Response>> getQuestionsByCreatedBy(String userId, int pageNo, int pageSize, String... sorts) {
        log.info("Getting questions V2 by created by user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findByCreatedById(userId, pageable);

        List<QuestionV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<List<QuestionV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    public PageResponse<List<QuestionV2Response>> searchQuestions(String keyword, int pageNo, int pageSize, String... sorts) {

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findByContentContaining(keyword, pageable);

        List<QuestionV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<List<QuestionV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    @Transactional
    public void deleteQuestion(String id) {
        if (!questionV2Repository.existsById(id)) {
            throw new AppException(ErrorCode.QUESTION_V2_NOT_FOUND);
        }

        questionV2Repository.deleteById(id);
    }

    private void validateQuestionAnswersRequest(List<AnswerV2Request> answers, String type) {
        if (Objects.isNull(answers)|| answers.isEmpty()) {
            throw new AppException(ErrorCode.INSUFFICIENT_ANSWERS_V2);
        }

        List<AnswerV2Request> correctAnswers = answers.stream()
                .filter(AnswerV2Request::getIsCorrect)
                .toList();

        if (correctAnswers.isEmpty()) {
            throw new AppException(ErrorCode.NO_CORRECT_ANSWER_V2);
        }

        if (type.equalsIgnoreCase(QuestionType.FRQ.getValue()) && correctAnswers.size() > 1) {
            throw new AppException(ErrorCode.MULTIPLE_CORRECT_ANSWERS_V2);
        }
    }


}
