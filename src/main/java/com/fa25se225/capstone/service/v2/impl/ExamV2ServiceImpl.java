package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.kafka.FrqGradingEvent;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.v2.request.*;
import com.fa25se225.capstone.dto.v2.response.*;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.*;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.service.v2.ExamV2Service;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExamV2ServiceImpl implements ExamV2Service {

    private final ExamTemplateV2Repository templateRepository;
    private final QuestionV2Repository questionRepository;
    private final ExamV2Repository examRepository;
    private final ExamAttemptV2Repository attemptRepository;
    private final StudentAnswerV2Repository studentAnswerRepository;
    private final ExamQuestionV2Repository examQuestionRepository;
    private final AnswerV2Repository answerRepository;

    private final ExamV2Mapper examV2Mapper;
    private final ExamAttemptV2Mapper examAttemptV2Mapper;
    private final ExamAttemptDetailMapper examAttemptDetailMapper;
    private final ExamQuestionDetailMapper examQuestionDetailMapper;
    private final StudentAnswerDetailMapper studentAnswerDetailMapper;
    private final SubjectV2Mapper subjectV2Mapper;
    private final AnswerV2Mapper answerV2Mapper;

    private final AccountUtil accountUtil;
    private final PageHelper pageHelper;
    private final FrqGradingProducerService gradingProducer;


    @Override
    @Transactional
    public ExamV2Response startExamFromTemplate(StartSingleExamRequest request) {
        log.info("Bắt đầu tạo bài thi lẻ từ template: {}", request.getTemplateId());
        ExamTemplateV2 template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        return handleExamStart(List.of(template));
    }

    @Override
    @Transactional
    public ExamV2Response startExamFromComboTemplates(StartComboExamRequest request) {
        List<String> templateIds = request.getTemplateIds();
        log.info("Bắt đầu tạo bài thi combo từ {} templates", templateIds.size());

        List<ExamTemplateV2> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND);
        }

        return handleExamStart(templates);
    }

    @Override
    @Transactional
    public ExamV2Response startRandomExamCombo(StartRandomComboRequest request) {
        List<String> subjectIds = request.getSubjectIds();
        log.info("Bắt đầu tạo bài thi combo ĐỀ XUẤT (Recommended) cho các môn: {}", subjectIds);

        List<ExamTemplateV2> templates = new ArrayList<>();
        Pageable topOne = PageRequest.of(0, 1);

        for (String subjectName : subjectIds) {
            ExamTemplateV2 template = templateRepository.findBySubjectIdAndIsActiveTrueOrderByAverageRatingDesc(subjectName, topOne)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));
            templates.add(template);
        }

        return handleExamStart(templates);
    }

    private ExamV2Response resumeExam(ExamAttemptV2 attempt) {
        ExamV2 exam = attempt.getExam();

        ExamV2Response response = examV2Mapper.toResponse(exam);
        response.setExamAttemptId(attempt.getId());

        List<StudentAnswerV2> savedAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attempt.getId());

        Map<String, StudentAnswerV2> answerMap = studentAnswersToMap(savedAnswers);

        response.getQuestions().forEach(examQuestionV2Response -> {
            StudentAnswerV2 savedAns = answerMap.get(examQuestionV2Response.getExamQuestionId());

            if (Objects.nonNull(savedAns)) {
                examQuestionV2Response.setSavedAnswer(studentAnswerDetailMapper.toResponse(savedAns));
            }

            var examAnswers = examQuestionV2Response.getQuestion().getAnswers();
            if (Objects.nonNull(examAnswers) && examAnswers.size() == 1) {
                examQuestionV2Response.getQuestion().setAnswers(null);
            }
        });

        return response;
    }

    private ExamV2Response handleExamStart(List<ExamTemplateV2> templates) {
        User currentUser = accountUtil.getCurrentUser();

        ExamTemplateV2 primaryTemplate = templates.get(0);

        Optional<ExamAttemptV2> existingAttempt = attemptRepository
                .findFirstByUserIdAndSourceTemplateIdAndStatus(
                        currentUser.getId(),
                        primaryTemplate.getId(),
                        AttemptStatusV2.IN_PROGRESS
                );

        if (existingAttempt.isPresent()) {
            log.info("Found an unfinished test (AttemptID: {}). Restoring...", existingAttempt.get().getId());
            return resumeExam(existingAttempt.get());
        }

        log.info("Unfinished test not found. Creating the new one...");
        return generateExamAndAttempt(templates);
    }

    private ExamV2Response generateExamAndAttempt(List<ExamTemplateV2> templates) {
        User currentUser = accountUtil.getCurrentUser();

        String examTitle = templates.size() > 1 ? "Bài thi tổ hợp" : templates.get(0).getTitle();
        String examDescription = templates.stream().map(ExamTemplateV2::getTitle).collect(Collectors.joining(", "));
        int totalDuration = templates.stream().mapToInt(ExamTemplateV2::getDuration).sum();
        int totalPassingScore = templates.stream().mapToInt(ExamTemplateV2::getPassingScore).sum();

        User teacher = templates.get(0).getCreatedBy();
        Subject subject = templates.get(0).getSubject();

        ExamV2 exam = ExamV2.builder()
                .title(examTitle)
                .description(examDescription)
                .subject(subject)
                .duration(totalDuration)
                .passingScore(totalPassingScore)
                .belongTo(teacher)
                .build();

        List<ExamRuleV2> allRules = templates.stream()
                .flatMap(t -> t.getRules().stream())
                .toList();

        List<ExamQuestionV2> generatedQuestions = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);

        for (ExamRuleV2 rule : allRules) {
            List<QuestionV2> randomQuestions = questionRepository.findRandomQuestionsByCriteria(
                    rule.getTopic().getId(),
                    rule.getQuestionType().getValue(),
                    rule.getDifficulty().getId(),
                    rule.getTemplate().getCreatedBy().getId(),
                    rule.getNumberOfQuestions()
            );

            if (randomQuestions.size() < rule.getNumberOfQuestions()) {
                log.warn("Không đủ câu hỏi cho rule: topic={}, diff={}, need={}, found={}",
                        rule.getTopic().getName(), rule.getDifficulty().getName(), rule.getNumberOfQuestions(), randomQuestions.size());
            }

            for (QuestionV2 q : randomQuestions) {
                generatedQuestions.add(ExamQuestionV2.builder()
                        .exam(exam)
                        .question(q)
                        .orderNumber(order.getAndIncrement())
                        .points(rule.getPoints())
                        .build());
            }
        }

        Collections.shuffle(generatedQuestions);
        for (int i = 0; i < generatedQuestions.size(); i++) {
            generatedQuestions.get(i).setOrderNumber(i + 1);
        }

        exam.setQuestions(generatedQuestions);
        ExamV2 savedExam = examRepository.save(exam);
        log.info("Đã tạo ExamV2 (ID: {}) với {} câu hỏi", savedExam.getId(), savedExam.getQuestions().size());

        Map<String, ExamTemplateV2> templateMap = templates.stream()
                .collect(Collectors.toMap(ExamTemplateV2::getId, Function.identity(), (o1, o2) -> o1));

        for(ExamTemplateV2 tpl : templateMap.values()){
            tpl.setTotalTakers(tpl.getTotalTakers() + 1);
        }
        templateRepository.saveAll(templateMap.values());

        ExamAttemptV2 attempt = ExamAttemptV2.builder()
                .exam(savedExam)
                .user(currentUser)
                .startTime(LocalDateTime.now())
                .status(AttemptStatusV2.IN_PROGRESS)
                .score(0.0)
                .sourceTemplate(templates.get(0))
                .build();

        attemptRepository.save(attempt);

        ExamV2Response response = examV2Mapper.toResponse(savedExam);
        response.setExamAttemptId(attempt.getId());

        response.getQuestions().forEach(examQuestion -> {
            var answers = examQuestion.getQuestion().getAnswers();
            if (Objects.nonNull(answers) && answers.size() == 1) {
                examQuestion.getQuestion().setAnswers(null);
            }
        });

        return response;
    }


    @Override
    @Transactional
    public SubmitAttemptV2Response gradeExamAttempt(String attemptId, SubmitAttemptV2Request request) {
        log.info("Bắt đầu luồng submit cho Lượt thi (Attempt): {}", attemptId);

        ExamAttemptV2 attempt = fetchAttemptAndRequireStatus(attemptId, AttemptStatusV2.IN_PROGRESS);

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(AttemptStatusV2.PENDING_GRADING);

        List<StudentAnswerV2> studentAnswers = new ArrayList<>();
        List<FrqGradingEvent> gradingTasks = new ArrayList<>();
        double mcqTotalScore = 0.0;
        int frqCount = 0;

        for (StudentAnswerV2Request dto : request.getAnswers()) {
            ExamQuestionV2 examQuestion = examQuestionRepository.findById(dto.getExamQuestionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

            QuestionV2 question = examQuestion.getQuestion();
            double maxPoints = examQuestion.getPoints();

            StudentAnswerV2 studentAnswer = StudentAnswerV2.builder()
                    .examAttempt(attempt)
                    .examQuestion(examQuestion)
                    .frqAnswerText(dto.getFrqAnswerText())
                    .build();

            if (dto.getSelectedAnswerId() != null) {
                AnswerV2 selectedAnswer = answerRepository.findById(dto.getSelectedAnswerId()).orElse(null);
                studentAnswer.setSelectedAnswer(selectedAnswer);
            }

            if (question.getType() == QuestionType.MCQ) {
                if (studentAnswer.getSelectedAnswer() != null && studentAnswer.getSelectedAnswer().getIsCorrect()) {
                    studentAnswer.setScore(maxPoints);
                    mcqTotalScore += maxPoints;
                } else {
                    studentAnswer.setScore(0.0);
                }
            } else if (question.getType() == QuestionType.FRQ) {
                studentAnswer.setScore(null);
                frqCount++;

                AnswerV2 modelAnswer = question.getAnswers().stream()
                        .filter(AnswerV2::getIsCorrect)
                        .findFirst()
                        .orElse(null);

                if (modelAnswer != null) {
                    gradingTasks.add(FrqGradingEvent.builder()
                            .studentAnswerId(studentAnswer.getId())
                            .attemptId(attemptId)
                            .modelAnswer(modelAnswer.getContent())
                            .studentAnswerText(studentAnswer.getFrqAnswerText())
                            .maxPoints(maxPoints)
                            .build());
                } else {
                    log.error("Không tìm thấy đáp án mẫu (model answer) cho câu hỏi FRQ ID: {}", question.getId());
                    studentAnswer.setScore(0.0);
                    studentAnswer.setFeedback("Error: No model answer found for grading.");
                }
            }
            studentAnswers.add(studentAnswer);
        }

        attempt.setScore(mcqTotalScore);

        if (frqCount == 0) {
            attempt.setStatus(AttemptStatusV2.COMPLETED);
        }

        studentAnswerRepository.saveAll(studentAnswers);

        List<FrqGradingEvent> finalGradingTasks = new ArrayList<>();
        int taskIndex = 0;
        for (StudentAnswerV2 sa : studentAnswers) {
            if(sa.getExamQuestion().getQuestion().getType() == QuestionType.FRQ && sa.getScore() == null) {
                if(taskIndex < gradingTasks.size()) {
                    FrqGradingEvent task = gradingTasks.get(taskIndex++);
                    finalGradingTasks.add(FrqGradingEvent.builder()
                            .studentAnswerId(sa.getId())
                            .attemptId(task.attemptId())
                            .modelAnswer(task.modelAnswer())
                            .studentAnswerText(task.studentAnswerText())
                            .maxPoints(task.maxPoints())
                            .build());
                }
            }
        }

        var savedAttempt = attemptRepository.save(attempt);

        if (!finalGradingTasks.isEmpty()) {
            log.info("Push {} grading event into Kafka for Attempt: {}", finalGradingTasks.size(), attemptId);
            finalGradingTasks.forEach(gradingProducer::sendGradingTask);
        }

        return SubmitAttemptV2Response.builder()
                .attemptId(savedAttempt.getId())
                .status(savedAttempt.getStatus())
                .build();
    }

    @Override
    public PageResponse<List<ExamAttemptV2Response>> getMyExamHistory(int pageNo, int pageSize, String... sorts) {
        User currentUser = accountUtil.getCurrentUser();
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);

        Page<ExamAttemptV2> page = attemptRepository.findByUserId(currentUser.getId(), pageable);

        List<ExamAttemptV2Response> responses = page.getContent().stream()
                .map(examAttemptV2Mapper::toResponse)
                .toList();

        return PageResponse.<List<ExamAttemptV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamAttemptDetailResponse getAttemptResultDetails(String attemptId) {
        User currentUser = accountUtil.getCurrentUser();

        ExamAttemptV2 attempt = attemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<StudentAnswerV2> studentAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);
        Map<String, StudentAnswerV2> studentAnswerMap = studentAnswersToMap(studentAnswers);

        ExamAttemptDetailResponse response = examAttemptDetailMapper.toResponse(attempt);

        Set<Subject> subjects = attempt.getExam().getQuestions().stream()
                .map(eq -> eq.getQuestion().getSubject())
                .collect(Collectors.toSet());
        response.setSubjects(subjects.stream().map(subjectV2Mapper::toResponse).toList());

        List<ExamQuestionDetailResponse> questionDetails = attempt.getExam().getQuestions().stream()
                .sorted(Comparator.comparing(ExamQuestionV2::getOrderNumber))
                .map(examQuestion -> {
                    ExamQuestionDetailResponse qdResponse = examQuestionDetailMapper.toResponse(examQuestion);

                    StudentAnswerV2 studentAnswer = studentAnswerMap.get(examQuestion.getId());
                    if (studentAnswer != null) {
                        StudentAnswerDetailResponse saResponse = studentAnswerDetailMapper.toResponse(studentAnswer);

                        AnswerV2 correctAnswer = examQuestion.getQuestion().getAnswers().stream()
                                .filter(AnswerV2::getIsCorrect)
                                .findFirst()
                                .orElse(null);

                        if(correctAnswer != null) {
                            saResponse.setCorrectAnswer(answerV2Mapper.toResponse(correctAnswer));
                        }

                        qdResponse.setStudentAnswer(saResponse);
                    }
                    return qdResponse;
                })
                .toList();

        response.setQuestions(questionDetails);
        return response;
    }

    @Override
    @Transactional
    public void rateAttempt(String attemptId, RateAttemptRequest request) {
        User currentUser = accountUtil.getCurrentUser();

        ExamAttemptV2 attempt = attemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (attempt.getRating() != null) {
            throw new AppException(ErrorCode.ALREADY_RATE);
        }

        if (attempt.getStatus() != AttemptStatusV2.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_EXAM_ATTEMPT_STATE);
        }

        attempt.setRating(request.getRating());
        attempt.setComment(request.getComment());
        attemptRepository.save(attempt);

        ExamTemplateV2 template = attempt.getSourceTemplate();
        if (template == null) {
            log.warn("Attempt {} không có sourceTemplate, không thể cập nhật rating.", attemptId);
            return;
        }

        int oldTotalRatings = template.getTotalRatings();
        double oldAverageRating = template.getAverageRating();

        int newTotalRatings = oldTotalRatings + 1;
        double newAverageRating = ((oldAverageRating * oldTotalRatings) + request.getRating()) / newTotalRatings;

        template.setAverageRating(newAverageRating);
        template.setTotalRatings(newTotalRatings);
        templateRepository.save(template);
    }

    @Override
    @Transactional
    public void saveExamProgress(String attemptId, SaveProgressRequest request) {
        ExamAttemptV2 attempt = fetchAttemptAndRequireStatus(attemptId, AttemptStatusV2.IN_PROGRESS);

        List<StudentAnswerV2> existingAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);
        Map<String, StudentAnswerV2> answerMap = studentAnswersToMap(existingAnswers);

        List<StudentAnswerV2> answersToSave = new ArrayList<>();

        for (StudentAnswerV2Request dto : request.getAnswers()) {
            StudentAnswerV2 studentAnswer = answerMap.get(dto.getExamQuestionId());

            if (studentAnswer == null) {
                ExamQuestionV2 examQuestion = examQuestionRepository.findById(dto.getExamQuestionId())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

                studentAnswer = StudentAnswerV2.builder()
                        .examAttempt(attempt)
                        .examQuestion(examQuestion)
                        .build();
            }

            if (dto.getSelectedAnswerId() != null) {
                AnswerV2 selectedAnswer = answerRepository.findById(dto.getSelectedAnswerId()).orElse(null);
                studentAnswer.setSelectedAnswer(selectedAnswer);
            } else {
                studentAnswer.setSelectedAnswer(null);
            }

            studentAnswer.setFrqAnswerText(dto.getFrqAnswerText());

            answersToSave.add(studentAnswer);
        }

        studentAnswerRepository.saveAll(answersToSave);

    }

    private Map<String, StudentAnswerV2> studentAnswersToMap(List<StudentAnswerV2> answers) {
        return answers.stream()
                .collect(Collectors.toMap(
                        sa -> sa.getExamQuestion().getId(),
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));
    }

    private ExamAttemptV2 fetchAttemptAndRequireStatus(String attemptId, AttemptStatusV2 requiredStatus) {
        ExamAttemptV2 attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        User currentUser = accountUtil.getCurrentUser();
        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (requiredStatus != null && attempt.getStatus() != requiredStatus) {
            throw new AppException(ErrorCode.INVALID_EXAM_ATTEMPT_STATE);
        }
        return attempt;
    }

    @Override
    @Transactional
    public ExamAttemptV2Response manualGradeAttempt(String attemptId, ManualGradeRequest request) {
        log.info("Teacher manually grade attempt: {}", attemptId);

        ExamAttemptV2 attempt = attemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        User currentTeacher = accountUtil.getCurrentUser();
        ExamTemplateV2 template = attempt.getSourceTemplate();

        // Only teacher who created this exam can grade
        if (Objects.isNull(template)|| !template.getCreatedBy().getId().equals(currentTeacher.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<StudentAnswerV2> studentAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);
        Map<String, StudentAnswerV2> answerMap = studentAnswersToMap(studentAnswers);

        for (ManualGradeRequest.GradeItem item : request.getGrades()) {
            StudentAnswerV2 answer = answerMap.get(item.getExamQuestionId());
            if (answer != null) {
                double maxPoints = answer.getExamQuestion().getPoints();
                if (item.getScore() > maxPoints || item.getScore() < 0) {
                    throw new AppException(ErrorCode.INVALID_SCORE);
                }

                answer.setScore(item.getScore());
                if (StringUtils.hasText(item.getFeedback())) {
                    answer.setFeedback(item.getFeedback());
                }
            }
        }

        studentAnswerRepository.saveAll(studentAnswers);

        double newTotalScore = studentAnswers.stream()
                .map(sa -> sa.getScore() != null ? sa.getScore() : 0.0)
                .mapToDouble(Double::doubleValue)
                .sum();

        attempt.setScore(newTotalScore);

        if (attempt.getStatus() == AttemptStatusV2.PENDING_GRADING) {
            attempt.setStatus(AttemptStatusV2.COMPLETED);

        }

        ExamAttemptV2 savedAttempt = attemptRepository.save(attempt);
        return examAttemptV2Mapper.toResponse(savedAttempt);
    }

    @Override
    @Transactional
    public void requestReview(String attemptId, RequestReviewRequest request) {
        ExamAttemptV2 attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        User currentUser = accountUtil.getCurrentUser();
        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (attempt.getStatus() != AttemptStatusV2.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_EXAM_ATTEMPT_STATE);
        }

        attempt.setStatus(AttemptStatusV2.REVIEW_REQUESTED);
        attempt.setReviewReason(request.getReason());
        attemptRepository.save(attempt);
    }

    @Override
    public PageResponse<List<ExamAttemptV2Response>> getAttemptsForTeacherReview(int pageNo, int pageSize, boolean includePending, boolean includeReviewRequested, String... sorts) {

        User teacher = accountUtil.getCurrentUser();
        List<AttemptStatusV2> statuses = new ArrayList<>();

        if (includePending){
            statuses.add(AttemptStatusV2.PENDING_GRADING);
        }
        if (includeReviewRequested){
            statuses.add(AttemptStatusV2.REVIEW_REQUESTED);
        }

        if (statuses.isEmpty()){
            return PageResponse.<List<ExamAttemptV2Response>>builder().items(List.of()).totalElement(0).build();
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<ExamAttemptV2> page = attemptRepository.findByTeacherAndStatusIn(teacher.getId(), statuses, pageable);

        List<ExamAttemptV2Response> items = page.getContent().stream()
                .map(examAttemptV2Mapper::toResponse)
                .toList();

        return PageResponse.<List<ExamAttemptV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .items(items)
                .build();
    }
}
