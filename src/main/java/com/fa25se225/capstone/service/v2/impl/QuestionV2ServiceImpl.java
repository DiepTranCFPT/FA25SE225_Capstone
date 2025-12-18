package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.AnswerV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionImportRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionUpdateV2Request;
import com.fa25se225.capstone.dto.v2.response.QuestionImportResponse;
import com.fa25se225.capstone.dto.v2.response.QuestionManageV2Response;
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
import com.fa25se225.capstone.repository.TemporaryFileRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.implementation.CloudinaryService;
import com.fa25se225.capstone.service.v2.QuestionV2Service;
import com.fa25se225.capstone.service.v2.QuestionImportService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
    private final TemporaryFileRepository temporaryFileRepository;
    private final QuestionImportService questionImportService;
    private final CloudinaryService cloudinaryService;

    private void confirmImageUsage(String imageUrl) {
        if (Strings.isNotEmpty(imageUrl)) {
            temporaryFileRepository.findByUrl(imageUrl)
                    .ifPresent(temporaryFileRepository::delete);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "questions", allEntries = true)
    public QuestionManageV2Response createQuestion(QuestionCreationV2Request request) {
        log.info("Creating new question V2: Type={}", request.getType());

        User currentUser = accountUtil.getCurrentUser();

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));

        QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));

        String imageUrl = request.getImageUrl();
        confirmImageUsage(imageUrl);

        QuestionV2 savedQuestion;

        if (Objects.nonNull(request.getSubQuestions())  && !request.getSubQuestions().isEmpty()) {
            savedQuestion = createGroupQuestion(request, imageUrl, currentUser, subject, difficulty, topic);
        } else {
            savedQuestion = createSingleQuestion(request, imageUrl, currentUser, subject, difficulty, topic);
        }

        return questionV2Mapper.toManageResponse(savedQuestion);
    }

    private QuestionV2 createSingleQuestion(QuestionCreationV2Request request, String imageUrl,
                                            User user, Subject subject, QuestionDifficultyV2 difficulty, QuestionTopicV2 topic) {
        if (Objects.isNull(request.getAnswers()) || request.getAnswers().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_SINGLE_QUESTION_V2);
        }
        validateQuestionAnswersRequest(request.getAnswers(), request.getType());

        QuestionV2 question = QuestionV2.builder()
                .content(request.getContent())
                .type(QuestionType.valueOf(request.getType()))
                .imageUrl(imageUrl)
                .subject(subject)
                .difficulty(difficulty)
                .topic(topic)
                .createdBy(user)
                .build();

        List<AnswerV2> answers = mapAnswers(request.getAnswers(), question);
        question.setAnswers(answers);

        return questionV2Repository.save(question);
    }

    private QuestionV2 createGroupQuestion(QuestionCreationV2Request request, String imageUrl,
                                           User user, Subject subject, QuestionDifficultyV2 difficulty, QuestionTopicV2 topic) {
        QuestionV2 parentQuestion = QuestionV2.builder()
                .content(request.getContent())
                .type(QuestionType.valueOf(request.getType()))
                .imageUrl(imageUrl)
                .subject(subject)
                .difficulty(difficulty)
                .topic(topic)
                .createdBy(user)
                .build();

        List<QuestionV2> subQuestions = new ArrayList<>();

        for (QuestionCreationV2Request subRequest : request.getSubQuestions()) {
            if (Objects.isNull(subRequest.getAnswers()) || subRequest.getAnswers().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_SUB_QUESTION_V2);
            }
            validateQuestionAnswersRequest(subRequest.getAnswers(), subRequest.getType());

            QuestionV2 subQ = QuestionV2.builder()
                    .content(subRequest.getContent())
                    .type(QuestionType.valueOf(subRequest.getType()))
                    .parent(parentQuestion)
                    .createdBy(user)
                    // Inherit
                    .subject(subject)
                    .topic(topic)
                    .difficulty(difficulty)
                    .build();


            List<AnswerV2> subAnswers = mapAnswers(subRequest.getAnswers(), subQ);
            subQ.setAnswers(subAnswers);

            subQuestions.add(subQ);
        }

        parentQuestion.setSubQuestions(subQuestions);

        //cascade
        return questionV2Repository.save(parentQuestion);
    }

    private List<AnswerV2> mapAnswers(List<AnswerV2Request> answerRequests, QuestionV2 question) {
        if(answerRequests == null) return new ArrayList<>();
        return answerRequests.stream()
                .map(dto -> {
                    AnswerV2 ans = answerV2Mapper.toEntity(dto);
                    ans.setQuestion(question);
                    return ans;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "question", key = "#id"),
            @CacheEvict(value = "questions", allEntries = true)
    })
    public QuestionManageV2Response updateQuestion(String id, QuestionUpdateV2Request request) {
        log.info("Updating question V2 with ID: {}", id);

        QuestionV2 existingQuestion = questionV2Repository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_V2_NOT_FOUND));

        updateQuestionMetadata(existingQuestion, request);

        //group
        if (request.getSubQuestions() != null && !request.getSubQuestions().isEmpty()) {
            updateGroupQuestionLogic(existingQuestion, request.getSubQuestions());
        }
        else {
            // Clear old subquestions if user change Group -> Single
            existingQuestion.getSubQuestions().clear();
            updateSingleQuestionAnswers(existingQuestion, request.getAnswers(), request.getType());
        }

        QuestionV2 updatedQuestion = questionV2Repository.save(existingQuestion);
        return questionV2Mapper.toManageResponse(updatedQuestion);
    }

    private void updateQuestionMetadata(QuestionV2 question, QuestionUpdateV2Request request) {
        if (StringUtils.hasText(request.getContent())) {
            question.setContent(request.getContent());
        }

        if (StringUtils.hasText(request.getType())) {
            try {
                question.setType(QuestionType.fromValue(request.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_QUESTION_V2_TYPE);
            }
        }

        if (request.getImageUrl() != null) {
            confirmImageUsage(request.getImageUrl());
            question.setImageUrl(request.getImageUrl());
        }
        if (request.getAudioUrl() != null) {
            question.setAudioUrl(request.getAudioUrl());
        }

        // Update References (Chỉ update nếu có gửi lên)
        if (StringUtils.hasText(request.getDifficultyName())) {
            QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_V2_NOT_FOUND));
            question.setDifficulty(difficulty);
        }

        if (StringUtils.hasText(request.getTopicName())) {
            QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND));
            question.setTopic(topic);
        }
    }

    private void updateSingleQuestionAnswers(QuestionV2 question, List<AnswerV2Request> newAnswers, String typeStr) {
        if (Objects.isNull(newAnswers)) return;

        validateQuestionAnswersRequest(newAnswers, typeStr != null ? typeStr : question.getType().toString());

        // Cách đơn giản nhất: Xóa hết cũ, thêm mới (JPA orphanRemoval sẽ lo việc delete DB)
        question.getAnswers().clear();

        List<AnswerV2> mappedAnswers = newAnswers.stream()
                .map(dto -> {
                    AnswerV2 ans = answerV2Mapper.toEntity(dto);
                    ans.setQuestion(question);
                    return ans;
                })
                .collect(Collectors.toList());

        question.getAnswers().addAll(mappedAnswers);
    }

    // UPDATE SUB-QUESTIONS
    private void updateGroupQuestionLogic(QuestionV2 parent, List<QuestionUpdateV2Request> subRequests) {
        Map<String, QuestionV2> currentSubMap = parent.getSubQuestions().stream()
                .collect(Collectors.toMap(QuestionV2::getId, Function.identity()));

        List<QuestionV2> updatedSubList = new ArrayList<>();

        for (QuestionUpdateV2Request subDto : subRequests) {
            QuestionV2 subQ;

            // UPDATE Existing
            if (StringUtils.hasText(subDto.getId()) && currentSubMap.containsKey(subDto.getId())) {
                subQ = currentSubMap.get(subDto.getId());

                if (subDto.getContent() != null) subQ.setContent(subDto.getContent());
                if (subDto.getType() != null) subQ.setType(QuestionType.valueOf(subDto.getType()));

                updateSingleQuestionAnswers(subQ, subDto.getAnswers(), subDto.getType());
            }
            // CREATE New
            else {
                subQ = QuestionV2.builder()
                        .content(subDto.getContent())
                        .type(QuestionType.valueOf(subDto.getType()))
                        .parent(parent)
                        // Inherit
                        .createdBy(parent.getCreatedBy())
                        .subject(parent.getSubject())
                        .topic(parent.getTopic())
                        .difficulty(parent.getDifficulty())
                        .build();

                List<AnswerV2> newAns = mapAnswers(subDto.getAnswers(), subQ);
                subQ.setAnswers(newAns);
            }

            updatedSubList.add(subQ);
        }

        // SYNC LIST
        parent.getSubQuestions().clear();
        parent.getSubQuestions().addAll(updatedSubList);
    }

    @Override
    @Cacheable(value = "question", key = "#id")
    public QuestionManageV2Response getQuestionById(String id) {
        log.info("Getting question V2 by ID: {}", id);

        QuestionV2 questionV2 = questionV2Repository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_V2_NOT_FOUND));

        return questionV2Mapper.toManageResponse(questionV2);
    }

    @Override
    @Cacheable(value = "questions", key = "#pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<QuestionManageV2Response>> getAllQuestions(int pageNo, int pageSize, String[] sorts) {
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findAll(pageable);

        List<QuestionManageV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toManageResponse)
                .toList();


        return PageResponse.<List<QuestionManageV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    @Cacheable(value = "questions", key = "'subject_' + #subjectId + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<QuestionManageV2Response>> getQuestionsBySubject(String subjectId, int pageNo, int pageSize, String[] sorts) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new AppException(ErrorCode.SUBJECT_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findBySubjectId(subjectId, pageable);

        List<QuestionManageV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toManageResponse)
                .toList();

        return PageResponse.<List<QuestionManageV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }



    @Override
    @Cacheable(value = "questions", key = "'topic_' + #topicId + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<QuestionManageV2Response>> getQuestionsByTopic(String topicId, int pageNo, int pageSize, String[] sorts) {
        if (!questionTopicV2Repository.existsById(topicId)) {
            throw new AppException(ErrorCode.QUESTION_TOPIC_V2_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findByTopicId(topicId, pageable);

        List<QuestionManageV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toManageResponse)
                .toList();

        return PageResponse.<List<QuestionManageV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    @Cacheable(value = "questions", key = "'user_' + #userId + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<QuestionManageV2Response>> getQuestionsByCreatedBy(String userId, int pageNo, int pageSize, String[] sorts) {
        log.info("Getting questions V2 by created by user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findByCreatedById(userId, pageable);

        List<QuestionManageV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toManageResponse)
                .toList();

        return PageResponse.<List<QuestionManageV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sorts)
                .totalElement(questionPage.getTotalElements())
                .totalPage(questionPage.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    @Cacheable(value = "questions", key = "'search_' + #keyword + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<QuestionManageV2Response>> searchQuestions(String keyword, int pageNo, int pageSize, String[] sorts) {

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionV2> questionPage = questionV2Repository.findByContentContaining(keyword, pageable);

        List<QuestionManageV2Response> responses = questionPage.getContent().stream()
                .map(questionV2Mapper::toManageResponse)
                .toList();

        return PageResponse.<List<QuestionManageV2Response>>builder()
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
    @Caching(evict = {
            @CacheEvict(value = "question", key = "#id"),
            @CacheEvict(value = "questions", allEntries = true)
    })
    public void deleteQuestion(String id) {
        QuestionV2 question = questionV2Repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_V2_NOT_FOUND));
        question.setDeleted(true);
        questionV2Repository.save(question);
    }

    @Override
    @Transactional
    @CacheEvict(value = "questions", allEntries = true)
    public void deleteQuestions(List<String> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return;
        }
        List<QuestionV2> questions = questionV2Repository.findAllById(ids);
        questions.forEach(question -> question.setDeleted(true));
        questionV2Repository.saveAll(questions);
    }

    @Override
    @Transactional
    @CacheEvict(value = "questions", allEntries = true)
    public QuestionImportResponse importQuestionsFromExcel(MultipartFile file, QuestionImportRequest request) {
        return questionImportService.importQuestionsFromExcel(file, request);
    }

    @Override
    public byte[] generateExampleTemplate() {
        return questionImportService.generateExampleTemplate();
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
