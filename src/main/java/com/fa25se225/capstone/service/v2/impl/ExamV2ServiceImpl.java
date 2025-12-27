package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.kafka.FrqGradingEvent;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.v2.request.*;
import com.fa25se225.capstone.dto.v2.response.*;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.*;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.service.TokenTransactionService;
import com.fa25se225.capstone.service.v2.ExamV2Service;
import com.fa25se225.capstone.service.NotificationService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
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

    private final TokenTransactionService tokenTransactionService;
    private final SecureRandom secureRandom;

    private final QuestionContextV2Repository contextRepository;
    private final NotificationService notificationService;


    @Override
    @Transactional
    @CacheEvict(value = {"exam_history", "exam_attempt_detail"}, allEntries = true)
    public ExamV2Response startExamFromTemplate(StartSingleExamRequest request) {
        log.info("Bắt đầu tạo bài thi lẻ từ template: {}", request.getTemplateId());
        ExamTemplateV2 template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        return handleExamStart(List.of(template));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"exam_history", "exam_attempt_detail"}, allEntries = true)
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
    @CacheEvict(value = {"exam_history", "exam_attempt_detail"}, allEntries = true)
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

    private ExamV2Response resumeExam(ExamAttemptV2 attempt, String newSessionToken) {
        ExamV2 exam = attempt.getExam();

        ExamV2Response response = examV2Mapper.toResponse(exam);
        response.setExamAttemptId(attempt.getId());
        response.setAttemptSessionToken(newSessionToken);

        if (exam.getDuration() != null && exam.getDuration() > 0) {
            // Lấy thời gian còn lại từ DB (đã được đóng băng nếu offline)
            response.setRemainTime(attempt.getRemainingTime() != null ? attempt.getRemainingTime() : 0L);
        }

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

        String newSessionToken = Integer.toString(100_000 + secureRandom.nextInt(900_000));

        Optional<ExamAttemptV2> existingAttempt = attemptRepository
                .findFirstByUserIdAndSourceTemplateIdAndStatus(
                        currentUser.getId(),
                        primaryTemplate.getId(),
                        AttemptStatusV2.IN_PROGRESS
                );
        ExamAttemptV2 attempt;

        if (existingAttempt.isPresent()) {
            log.info("Found an unfinished test (AttemptID: {}). Restoring...", existingAttempt.get().getId());
            attempt = existingAttempt.get();
            attempt.setAttemptSessionToken(newSessionToken);

            // FIX: Xử lý tương thích cho dữ liệu cũ (khi remainingTime bị NULL)
            if (attempt.getRemainingTime() == null && attempt.getExam().getDuration() != null && attempt.getExam().getDuration() > 0) {
                long durationSeconds = attempt.getExam().getDuration() * 60L;
                long elapsedSeconds = Duration.between(attempt.getStartTime(), LocalDateTime.now()).getSeconds();
                attempt.setRemainingTime(Math.max(0, durationSeconds - elapsedSeconds));
            }

            // Cập nhật thời gian trôi qua (tính toán trừ thời gian theo logic đóng băng) trước khi reset session
            updateTimeProgress(attempt);
            
            attempt = attemptRepository.save(attempt);
            return resumeExam(attempt, newSessionToken);
        }

        log.info("Unfinished test not found. Creating the new one...");
        return generateExamAndAttempt(templates, newSessionToken);
    }

    public ExamV2Response generateExamAndAttempt(List<ExamTemplateV2> templates, String newSessionToken) {
        User currentUser = accountUtil.getCurrentUser();

        // 1. Metadata Exam
        String examTitle = templates.size() > 1 ? "Bài thi tổ hợp" : templates.get(0).getTitle();
        String examDescription = templates.stream().map(ExamTemplateV2::getTitle).collect(Collectors.joining(", "));
        int totalDuration = templates.stream().mapToInt(ExamTemplateV2::getDuration).sum();
        int totalPassingScore = templates.stream().mapToInt(ExamTemplateV2::getPassingScore).sum();

        // 2. (Payment)
        for (ExamTemplateV2 template : templates) {
            BigDecimal templateCost = template.getTokenCost();
            User teacher = template.getCreatedBy();
            if (templateCost != null && templateCost.compareTo(BigDecimal.ZERO) > 0) {
                if (!currentUser.getId().equals(teacher.getId())) {
                    log.info("Processing exam payment for template '{}': user={}, teacher={}, amount={}",
                            template.getTitle(), currentUser.getId(), teacher.getId(), templateCost);
                    tokenTransactionService.processExamPayment(
                            currentUser.getId(), teacher.getId(), templateCost, "Exam: " + template.getTitle());
                }
            }
        }

        User teacher = templates.get(0).getCreatedBy();
        Subject subject = templates.get(0).getSubject();

        // 3. Init Exam Shell
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

        // Dùng List<List> để gom nhóm câu hỏi nhằm mục đích shuffle theo khối
        // List<ExamQuestionV2> bên trong là 1 khối (Block)
        List<List<ExamQuestionV2>> questionBlocks = new ArrayList<>();

//        for (ExamRuleV2 rule : allRules) {
//            double pointsPerQuestion = rule.getPoints();
//
//            if (rule.getNumberOfContexts() != null && rule.getNumberOfContexts() > 0) {
//
//                // 1. Random ra danh sách Context ID (Candidate Contexts)
//                List<String> contextIds = questionRepository.findRandomContextIds(
//                        rule.getTopic().getId(),
//                        rule.getQuestionType().getValue(),
//                        rule.getDifficulty().getId(),
//                        rule.getTemplate().getCreatedBy().getId(),
//                        rule.getNumberOfContexts()
//                );
//
//                if (contextIds.size() < rule.getNumberOfContexts()) {
//                    log.warn("Không đủ bài đọc (Context) cho rule: Topic={}, Yêu cầu {}, tìm thấy {}",
//                            rule.getTopic().getName(), rule.getNumberOfContexts(), contextIds.size());
//                }
//
//                // 2. Duyệt từng Context để lấy câu hỏi bên trong
//                for (String ctxId : contextIds) {
//                    QuestionContextV2 context = contextRepository.findByIdWithQuestions(ctxId).orElse(null);
//
//                    if (context != null) {
//                        List<QuestionV2> validQuestions = context.getQuestions().stream()
//                                .filter(q -> !q.getDeleted())
//                                .filter(q -> q.getType() == rule.getQuestionType())
//                                .filter(q -> q.getDifficulty().getId().equals(rule.getDifficulty().getId()))
//                                .filter(q -> q.getTopic().getId().equals(rule.getTopic().getId()))
//                                .collect(Collectors.toList());
//
//                        if (validQuestions.isEmpty()) {
//                            continue;
//                        }
//
//                        // 3. Xử lý số lượng câu hỏi TRONG mỗi Context
//                        Collections.shuffle(validQuestions);
//
//                        int limitPerContext = rule.getNumberOfQuestions();
//                        List<QuestionV2> selectedQuestions = validQuestions.stream()
//                                .limit(limitPerContext)
//                                .toList();
//
//                        // 4. Map sang ExamQuestion và thêm thành 1 khối
//                        if (!selectedQuestions.isEmpty()) {
//                            List<ExamQuestionV2> block = new ArrayList<>();
//                            for (QuestionV2 q : selectedQuestions) {
//                                block.add(ExamQuestionV2.builder()
//                                        .exam(exam)
//                                        .question(q)
//                                        .points(pointsPerQuestion)
//                                        .build());
//                            }
//                            questionBlocks.add(block);
//                        }
//                    }
//                }
//            }
//
//            // (SINGLE)
//            else {
//                List<QuestionV2> randomSingles = questionRepository.findRandomSingleQuestions(
//                        rule.getTopic().getId(),
//                        rule.getQuestionType().getValue(),
//                        rule.getDifficulty().getId(),
//                        rule.getTemplate().getCreatedBy().getId(),
//                        rule.getNumberOfQuestions()
//                );
//
//                if (randomSingles.size() < rule.getNumberOfQuestions()) {
//                    log.warn("Không đủ câu đơn cho rule: topic={}, diff={}, need={}, found={}",
//                            rule.getTopic().getName(), rule.getDifficulty().getName(),
//                            rule.getNumberOfQuestions(), randomSingles.size());
//                }
//
//                // Every single question is a separate block
//                for (QuestionV2 q : randomSingles) {
//                    List<ExamQuestionV2> block = new ArrayList<>();
//                    block.add(ExamQuestionV2.builder()
//                            .exam(exam)
//                            .question(q)
//                            .points(pointsPerQuestion)
//                            .build());
//                    questionBlocks.add(block);
//                }
//            }


        for (ExamRuleV2 rule : allRules) {
            double pointsPerQuestion = rule.getPoints();

            // --- is Explicit (exactly the number of context) or auto fill
            boolean isExplicitContextMode = rule.getNumberOfContexts() != null && rule.getNumberOfContexts() > 0;

            // Mục tiêu tổng số câu hỏi cho Rule này
            int targetTotalQuestions;
            if (isExplicitContextMode) {
                // Chế độ cũ: (Số bài đọc) * (Số câu mỗi bài)
                // Lưu ý: Trong chế độ cũ, rule.getNumberOfQuestions() đóng vai trò là "Limit Per Context"
                targetTotalQuestions = rule.getNumberOfContexts() * rule.getNumberOfQuestions();
            } else {
                // Chế độ Auto-fill: rule.getNumberOfQuestions() là Tổng số câu cần tìm
                targetTotalQuestions = rule.getNumberOfQuestions();
            }

            int currentCount = 0; // Đếm số câu đã lấy được cho rule này

            // BƯỚC 1: XỬ LÝ CÂU HỎI CÓ CONTEXT (Priority 1)
            // Nếu là Explicit Mode: Lấy đúng số lượng Context quy định.
            // Nếu là Auto-fill Mode: Lấy dư ra (ví dụ max 20 bài) để có đủ nguồn câu hỏi chọn lọc.
            int contextLimitQuery = isExplicitContextMode ? rule.getNumberOfContexts() : 20;

            List<String> contextIds = questionRepository.findRandomContextIds(
                    rule.getTopic().getId(),
                    rule.getQuestionType().getValue(),
                    rule.getDifficulty().getId(),
                    rule.getTemplate().getCreatedBy().getId(),
                    contextLimitQuery
            );

            // Duyệt qua từng Context tìm được
            for (String ctxId : contextIds) {
                // Nếu Auto-fill mà đã đủ câu -> Dừng ngay
                if (!isExplicitContextMode && currentCount >= targetTotalQuestions) {
                    break;
                }

                QuestionContextV2 context = contextRepository.findByIdWithQuestions(ctxId).orElse(null);
                if (context == null) continue;

                // Lọc câu hỏi valid trong Context
                List<QuestionV2> validQuestions = context.getQuestions().stream()
                        .filter(q -> !q.getDeleted())
                        .filter(q -> q.getType() == rule.getQuestionType())
                        .filter(q -> q.getDifficulty().getId().equals(rule.getDifficulty().getId()))
                        .filter(q -> q.getTopic().getId().equals(rule.getTopic().getId()))
                        .collect(Collectors.toList());

                if (validQuestions.isEmpty()) continue;

                // Shuffle câu hỏi trong bài đọc này
                Collections.shuffle(validQuestions);

                // Quyết định số lượng lấy từ bài này
                int takeFromThisContext;
                if (isExplicitContextMode) {
                    // Lấy tối đa theo quy định "Số câu mỗi bài"
                    takeFromThisContext = Math.min(validQuestions.size(), rule.getNumberOfQuestions());
                } else {
                    // Lấy tối đa số câu còn thiếu ("Greedy")
                    int remainingNeeded = targetTotalQuestions - currentCount;
                    takeFromThisContext = Math.min(validQuestions.size(), remainingNeeded);
                }

                List<QuestionV2> selectedQuestions = validQuestions.subList(0, takeFromThisContext);

                // Map và thêm vào Block
                if (!selectedQuestions.isEmpty()) {
                    List<ExamQuestionV2> block = new ArrayList<>();
                    for (QuestionV2 q : selectedQuestions) {
                        block.add(ExamQuestionV2.builder()
                                .exam(exam)
                                .question(q)
                                .points(pointsPerQuestion)
                                .build());
                    }
                    questionBlocks.add(block);
                    currentCount += selectedQuestions.size();
                }
            }

            // BƯỚC 2: XỬ LÝ CÂU HỎI ĐƠN (Single Questions) - (Priority 2 / Fallback)
            // Chỉ chạy bước này nếu:
            // 1. Đang ở chế độ Auto-fill (isExplicitContextMode = false)
            // 2. Vẫn chưa đủ số câu hỏi yêu cầu (remaining > 0)
            // (Lưu ý: Chế độ Explicit Mode thường không mix câu đơn vào, trừ khi bạn muốn đổi logic đó)

            int remainingNeeded = targetTotalQuestions - currentCount;

            if (!isExplicitContextMode && remainingNeeded > 0) {
                List<QuestionV2> randomSingles = questionRepository.findRandomSingleQuestions(
                        rule.getTopic().getId(),
                        rule.getQuestionType().getValue(),
                        rule.getDifficulty().getId(),
                        rule.getTemplate().getCreatedBy().getId(),
                        remainingNeeded
                );

                // Mỗi câu đơn là 1 block riêng
                for (QuestionV2 q : randomSingles) {
                    List<ExamQuestionV2> block = new ArrayList<>();
                    block.add(ExamQuestionV2.builder()
                            .exam(exam)
                            .question(q)
                            .points(pointsPerQuestion)
                            .build());
                    questionBlocks.add(block);
                    currentCount++;
                }
            }

            // Log warning nếu vẫn không đủ
            if (currentCount < targetTotalQuestions) {
                log.warn("Rule {}: Cần {} câu, chỉ tìm được {} câu (Topic: {})",
                        rule.getId(), targetTotalQuestions, currentCount, rule.getTopic().getName());
            }
        }

        // FLATTEN
        // Shuffle the order in block
        Collections.shuffle(questionBlocks);

        List<ExamQuestionV2> finalQuestionsToSave = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);

        for (List<ExamQuestionV2> block : questionBlocks) {
            for (ExamQuestionV2 eq : block) {
                eq.setOrderNumber(order.getAndIncrement());
                finalQuestionsToSave.add(eq);
            }
        }

        exam.setQuestions(finalQuestionsToSave);
        ExamV2 savedExam = examRepository.save(exam);
        log.info("Đã tạo ExamV2 (ID: {}) với {} câu hỏi từ {} blocks", savedExam.getId(), finalQuestionsToSave.size(), questionBlocks.size());

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
                .attemptSessionToken(newSessionToken)
                .score(0.0)
                .sourceTemplate(templates.get(0))
                // Khởi tạo thời gian còn lại (đổi phút sang giây)
                .remainingTime(savedExam.getDuration() != null ? savedExam.getDuration() * 60L : null)
                .lastInteractionTime(LocalDateTime.now())
                .build();

        attemptRepository.save(attempt);

        ExamV2Response response = examV2Mapper.toResponse(savedExam);
        response.setExamAttemptId(attempt.getId());
        response.setAttemptSessionToken(newSessionToken);

        response.getQuestions().forEach(examQuestion -> {
            var answers = examQuestion.getQuestion().getAnswers();
            if (Objects.nonNull(answers) && answers.size() == 1) {
                examQuestion.getQuestion().setAnswers(null);
            }
        });


        if (savedExam.getDuration() != null && savedExam.getDuration() > 0) {
            response.setRemainTime(attempt.getRemainingTime());
        }

        return response;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "exam_attempt_detail", key = "#attemptId"),
            @CacheEvict(value = "exam_history", allEntries = true)
    })
    public SubmitAttemptV2Response gradeExamAttempt(String attemptId, SubmitAttemptV2Request request) {
        log.info("Bắt đầu luồng submit cho Lượt thi (Attempt): {}", attemptId);

        ExamAttemptV2 attempt = fetchAttemptAndRequireStatus(attemptId, AttemptStatusV2.IN_PROGRESS);

        if (!attempt.getAttemptSessionToken().equals(request.getAttemptSessionToken())) {
            throw new AppException(ErrorCode.CONCURRENT_LOGIN_DETECTED);
        }

        ExamV2 exam = attempt.getExam();
        Integer durationMinutes = exam.getDuration();

        if (Objects.nonNull(durationMinutes) && durationMinutes > 0) {
            //give 1 minute more to submit
            long gracePeriodSeconds = 60;
            
            if (attempt.getRemainingTime() != null && attempt.getRemainingTime() < -gracePeriodSeconds) {
                log.warn("User submitted late! AttemptID: {}. Remaining Time: {}", attemptId, attempt.getRemainingTime());
                attempt.setIsLate(true);
            }
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(AttemptStatusV2.PENDING_GRADING);

        List<StudentAnswerV2> existingAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);

        Map<String, StudentAnswerV2> answerMap = existingAnswers.stream()
                .collect(Collectors.toMap(
                        sa -> sa.getExamQuestion().getId(),
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("Detected DUPLICATE answers for question {}. Merging...", existing.getExamQuestion().getId());
                            return existing;
                        }
                ));

        List<StudentAnswerV2> finalAnswersToSave = new ArrayList<>();
        List<FrqGradingEvent> gradingTasks = new ArrayList<>();
        double mcqTotalScore = 0.0;
        int frqCount = 0;

        for (StudentAnswerV2Request dto : request.getAnswers()) {

            StudentAnswerV2 studentAnswer = answerMap.get(dto.getExamQuestionId());
            ExamQuestionV2 examQuestion;

            if (studentAnswer == null) {
                examQuestion = examQuestionRepository.findById(dto.getExamQuestionId())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

                studentAnswer = StudentAnswerV2.builder()
                        .examAttempt(attempt)
                        .examQuestion(examQuestion)
                        .build();
            } else {
                examQuestion = studentAnswer.getExamQuestion();
            }

            if (dto.getSelectedAnswerId() != null) {
                AnswerV2 selectedAnswer = answerRepository.findById(dto.getSelectedAnswerId()).orElse(null);
                studentAnswer.setSelectedAnswer(selectedAnswer);
            } else {
                studentAnswer.setSelectedAnswer(null);
            }
            studentAnswer.setFrqAnswerText(dto.getFrqAnswerText());

            QuestionV2 question = examQuestion.getQuestion();
            double maxPoints = examQuestion.getPoints();

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
                }
            }

            finalAnswersToSave.add(studentAnswer);
        }

        List<StudentAnswerV2> savedAnswers = studentAnswerRepository.saveAll(finalAnswersToSave);


        for (StudentAnswerV2 sa : savedAnswers) {
            QuestionV2 question = sa.getExamQuestion().getQuestion();

            if (question.getType() == QuestionType.FRQ) {

                AnswerV2 modelAnswer = question.getAnswers().stream()
                        .filter(AnswerV2::getIsCorrect)
                        .findFirst()
                        .orElse(null);

                if (modelAnswer != null) {
                    String contextTitle = null;
                    String contextContent = null;

                    if (question.getContext() != null) {
                        contextTitle = question.getContext().getTitle();
                        contextContent = question.getContext().getContent();
                    }

                    gradingTasks.add(FrqGradingEvent.builder()
                            .studentAnswerId(sa.getId())
                            .attemptId(attemptId)
                            .modelAnswer(modelAnswer.getContent())
                            .studentAnswerText(sa.getFrqAnswerText())
                            .maxPoints(sa.getExamQuestion().getPoints())

                            .questionContent(question.getContent())
                            .contextTitle(contextTitle)
                            .contextContent(contextContent)
                            .build());
                }
            }
        }


        attempt.setScore(mcqTotalScore);
        if (frqCount == 0) attempt.setStatus(AttemptStatusV2.COMPLETED);

        var savedAttempt = attemptRepository.save(attempt);

        if (!gradingTasks.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    gradingTasks.forEach(gradingProducer::sendGradingTask);
                }
            });
        }
        attempt.setAttemptSessionToken(null);

        return SubmitAttemptV2Response.builder()
                .attemptId(savedAttempt.getId())
                .status(savedAttempt.getStatus())
                .build();
    }
    @Override
    @Cacheable(value = "exam_history", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamAttemptV2Response>> getMyExamHistory(int pageNo, int pageSize, String[] sorts) {
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

    private boolean isDoneByCurrentStudent(ExamAttemptV2 attempt, User user){
        return  attempt.getUser().getId().equals(user.getId());
    }

    private boolean isCreateByCurrentTeacher(ExamAttemptV2 attempt, User user){
        return  attempt.getSourceTemplate().getCreatedBy().getId().equals(user.getId());
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "exam_attempt_detail", key = "#attemptId")
    public ExamAttemptDetailResponse getAttemptResultDetails(String attemptId) {
        User currentUser = accountUtil.getCurrentUser();

        ExamAttemptV2 attempt = attemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        if (!isDoneByCurrentStudent(attempt, currentUser) && !isCreateByCurrentTeacher(attempt, currentUser)) {
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
    @Caching(evict = {
            @CacheEvict(value = "exam_attempt_detail", key = "#attemptId"),
            @CacheEvict(value = "exam_history", allEntries = true)
    })
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
        attempt.setRatingTime(LocalDateTime.now());
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

        if (!attempt.getAttemptSessionToken().equals(request.getAttemptSessionToken())) {
            throw new AppException(ErrorCode.CONCURRENT_LOGIN_DETECTED);
        }

        List<StudentAnswerV2> existingAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);
        Map<String, StudentAnswerV2> answerMap = studentAnswersToMap(existingAnswers);

        // --- LOGIC ADAPTIVE HEARTBEAT & FREEZE TIME ---
        updateTimeProgress(attempt);
        // --------------------------------------------------

        List<StudentAnswerV2> toSave = new ArrayList<>();
        for (StudentAnswerV2Request dto : request.getAnswers()) {
            StudentAnswerV2 answer = answerMap.get(dto.getExamQuestionId());

            if (answer == null) {
                ExamQuestionV2 examQuestion = examQuestionRepository.findById(dto.getExamQuestionId())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

                answer = StudentAnswerV2.builder()
                        .examAttempt(attempt)
                        .examQuestion(examQuestion)
                        .build();
            }

            if (dto.getSelectedAnswerId() != null) {
                AnswerV2 selected = answerRepository.findById(dto.getSelectedAnswerId()).orElse(null);
                answer.setSelectedAnswer(selected);
            } else {
                answer.setSelectedAnswer(null);
            }
            answer.setFrqAnswerText(dto.getFrqAnswerText());

            toSave.add(answer);
        }

        try {
            studentAnswerRepository.saveAll(toSave);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent save detected for attempt {}", attemptId);
        }

    }

    /**
     * Hàm tính toán và trừ thời gian làm bài dựa trên khoảng cách giữa các lần tương tác.
     * Hỗ trợ logic "Đóng băng thời gian" khi mất kết nối.
     */
    private void updateTimeProgress(ExamAttemptV2 attempt) {
        if (attempt.getExam().getDuration() != null && attempt.getExam().getDuration() > 0) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastInteraction = attempt.getLastInteractionTime();

            if (lastInteraction == null) {
                lastInteraction = attempt.getStartTime();
            }

            long actualDuration = Duration.between(lastInteraction, now).getSeconds();
            
            // Nếu actualDuration quá nhỏ (ví dụ < 0 do đồng hồ hệ thống lệch), bỏ qua
            if (actualDuration < 0) actualDuration = 0;

            // 1. Parse lịch sử các khoảng thời gian trước đó
            List<Long> intervals = new ArrayList<>();
            if (StringUtils.hasText(attempt.getInteractionIntervals())) {
                String[] parts = attempt.getInteractionIntervals().split(",");
                for (String p : parts) {
                    try {
                        intervals.add(Long.parseLong(p));
                    } catch (NumberFormatException ignored) {}
                }
            }

            // 2. Tính trung bình động (Moving Average)
            double averageInterval = 15.0; 
            if (!intervals.isEmpty()) {
                averageInterval = intervals.stream().mapToLong(val -> val).average().orElse(15.0);
            }

            // 3. Xác định ngưỡng (Threshold)
            long threshold = (long) (averageInterval * 1.5) + 2;

            long timeToDeduct;

            if (actualDuration <= threshold) {
                // Online bình thường
                timeToDeduct = actualDuration;
                
                intervals.add(actualDuration);
                if (intervals.size() > 5) intervals.remove(0);
                attempt.setInteractionIntervals(intervals.stream().map(String::valueOf).collect(Collectors.joining(",")));
            } else {
                // Offline/Lag -> Đóng băng (chỉ trừ threshold)
                timeToDeduct = threshold;
            }

            long currentRemaining = attempt.getRemainingTime() != null ? attempt.getRemainingTime() : 0;
            attempt.setRemainingTime(Math.max(0, currentRemaining - timeToDeduct));
            attempt.setLastInteractionTime(now);

        }
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
    @Caching(evict = {
            @CacheEvict(value = "exam_attempt_detail", key = "#attemptId"),
            @CacheEvict(value = "exam_history", allEntries = true)
    })
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

        String message = "Your exam attempt has been graded successfully.";
        notificationService.sendNotify(savedAttempt.getUser().getEmail(), message, savedAttempt.getExam().getTitle());

        return examAttemptV2Mapper.toResponse(savedAttempt);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "exam_attempt_detail", key = "#attemptId"),
            @CacheEvict(value = "exam_history", allEntries = true)
    })
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

        if (attempt.getSourceTemplate() != null) {
            User teacher = attempt.getSourceTemplate().getCreatedBy();
            String message = String.format("Student %s %s has requested a review for exam: %s",
                    currentUser.getFirstName(), currentUser.getLastName(), attempt.getExam().getTitle());
            notificationService.sendNotify(teacher.getEmail(), message, attemptId);
        }
    }

    @Override
    @Cacheable(value = "teacher_review_list", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageNo + '_' + #pageSize + '_' + #includePending + '_' + #includeReviewRequested + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamAttemptV2Response>> getAttemptsForTeacherReview(int pageNo, int pageSize, boolean includePending, boolean includeReviewRequested, String[] sorts) {

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

    @Override
    public PageResponse<List<ExamAttemptV2Response>> getAllStudentExamAttempts(int pageNo, int pageSize, String[] sorts) {
        User currentUser = accountUtil.getCurrentUser();
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);

        Page<ExamAttemptV2> page = attemptRepository.findByTeacher(currentUser, pageable);

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

}
