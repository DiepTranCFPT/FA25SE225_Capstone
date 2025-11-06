package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.v2.request.StudentAnswerV2Request;
import com.fa25se225.capstone.dto.v2.request.SubmitAttemptV2Request;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.ExamV2Response;
import com.fa25se225.capstone.dto.v2.response.GradingUserAnswerAIResponse;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.ExamV2Mapper;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.service.GeminiService;
import com.fa25se225.capstone.service.v2.ExamV2Service;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExamV2ServiceImpl implements ExamV2Service {

    private final ExamTemplateV2Repository templateRepository;
    private final QuestionV2Repository questionRepository;
    private final ExamV2Repository examRepository;
    private final ExamAttemptV2Repository attemptRepository;
    private final AccountUtil accountUtil;
    private final ExamV2Mapper examV2Mapper;

    private final StudentAnswerV2Repository studentAnswerRepository;
    private final ExamQuestionV2Repository examQuestionRepository;
    private final AnswerV2Repository answerRepository;
    private final ChatClient chatClient;


    @Override
    @Transactional
    public ExamV2Response startExamFromTemplate(String templateId) {
        log.info("Bắt đầu tạo bài thi từ template: {}", templateId);
        User currentUser = accountUtil.getCurrentUser();

        ExamTemplateV2 template = templateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_TEMPLATE_NOT_FOUND));

        User teacher = template.getCreatedBy();

        ExamV2 exam = ExamV2.builder()
                .title(template.getTitle())
                .description(template.getDescription())
                .subject(template.getSubject())
                .duration(template.getDuration())
                .passingScore(template.getPassingScore())
                .belongTo(teacher)
                .build();

        List<ExamQuestionV2> generatedQuestions = new ArrayList<>();
        AtomicInteger order = new AtomicInteger(1);

        for (ExamRuleV2 rule : template.getRules()) {
            List<QuestionV2> randomQuestions = questionRepository.findRandomQuestionsByCriteria(
                    rule.getTopic().getId(),
                    rule.getQuestionType().getValue(),
                    rule.getDifficulty().getId(),
                    teacher.getId(),
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
        for(int i = 0; i < generatedQuestions.size(); i++) {
            generatedQuestions.get(i).setOrderNumber(i + 1);
        }

        exam.setQuestions(generatedQuestions);
        ExamV2 savedExam = examRepository.save(exam);
        log.info("Đã tạo ExamV2 (ID: {}) với {} câu hỏi", savedExam.getId(), savedExam.getQuestions().size());

        ExamAttemptV2 attempt = ExamAttemptV2.builder()
                .exam(savedExam)
                .user(currentUser)
                .startTime(LocalDateTime.now())
                .status(AttemptStatusV2.IN_PROGRESS)
                .score(0.0)
                .build();

        attemptRepository.save(attempt);
        ExamV2Response response = examV2Mapper.toResponse(savedExam);
        response.setExamAttemptId(attempt.getId());
        return response;
    }

    @Transactional
    public ExamAttemptV2Response gradeExamAttempt(String attemptId, SubmitAttemptV2Request request) {
        log.info("Bắt đầu chấm điểm cho Lượt thi (Attempt): {}", attemptId);

        ExamAttemptV2 attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_ATTEMPT_NOT_FOUND));

        if (attempt.getStatus() != AttemptStatusV2.IN_PROGRESS) {
            throw new AppException(ErrorCode.INVALID_EXAM_ATTEMPT_STATE);
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(AttemptStatusV2.PENDING_GRADING);

        List<StudentAnswerV2> studentAnswers = new ArrayList<>();
        double totalScore = 0.0;

        for (StudentAnswerV2Request dto : request.getAnswers()) {
            ExamQuestionV2 examQuestion = examQuestionRepository.findById(dto.getExamQuestionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

            StudentAnswerV2 studentAnswer = StudentAnswerV2.builder()
                    .examAttempt(attempt)
                    .examQuestion(examQuestion)
                    .frqAnswerText(dto.getFrqAnswerText())
                    .score(0.0) // Điểm ban đầu
                    .build();

            if (dto.getSelectedAnswerId() != null) {
                AnswerV2 selectedAnswer = answerRepository.findById(dto.getSelectedAnswerId()).orElse(null);
                studentAnswer.setSelectedAnswer(selectedAnswer);
            }
            studentAnswers.add(studentAnswer);
        }
        studentAnswerRepository.saveAll(studentAnswers);
        log.info("Đã lưu {} câu trả lời của sinh viên.", studentAnswers.size());

        // 2. Chấm điểm
        for (StudentAnswerV2 sa : studentAnswers) {
            double score = 0.0;
            QuestionV2 question = sa.getExamQuestion().getQuestion();
            double maxPoints = sa.getExamQuestion().getPoints();

            if (question.getType() == QuestionType.MCQ) {
                if (sa.getSelectedAnswer() != null && sa.getSelectedAnswer().getIsCorrect()) {
                    score = maxPoints;
                }
            } else if (question.getType() == QuestionType.FRQ) {
                score = gradeFrqWithAI(question, sa.getFrqAnswerText(), maxPoints, sa);
            }

            sa.setScore(score);
            totalScore += score;
        }

        attempt.setScore(totalScore);
        attempt.setStatus(AttemptStatusV2.COMPLETED);

        studentAnswerRepository.saveAll(studentAnswers);
        var savedAttempt = attemptRepository.save(attempt);
        return ExamAttemptV2Response.builder()
                .examId(savedAttempt.getExam().getId())
                .id(savedAttempt.getId())
                .startAt(savedAttempt.getCreatedAt())
                .score(attempt.getScore())
                .endAt(savedAttempt.getEndTime())
                .doneBy(savedAttempt.getUser().getEmail())
                .build();
    }


    private double gradeFrqWithAI(QuestionV2 question, String studentAnswerText, double maxPoints, StudentAnswerV2 answerEntity) {
        if (!StringUtils.hasText(studentAnswerText)) {
            return 0.0;
        }


        AnswerV2 modelAnswer = question.getAnswers().stream()
                .filter(AnswerV2::getIsCorrect)
                .findFirst()
                .orElse(null);

        if (modelAnswer == null) {
            log.error("Không tìm thấy đáp án mẫu (model answer) cho câu hỏi FRQ ID: {}", question.getId());
            return 0.0; // Không có đáp án mẫu, không chấm được
        }

        String systemPrompt = String.format(
                "You are an AI grading assistant. Grade the student's answer based on the model answer and the maximum points. " +
                        "The score must be a number between 0 and %.1f.\n\n" +
                        "--- MODEL ANSWER ---\n%s\n\n" +
                        "--- MAXIMUM POINTS: %.1f ---",
                maxPoints, modelAnswer.getContent(), maxPoints
        );

        var aiResponse = chatClient.prompt().system(systemPrompt)
                .user(studentAnswerText)
                .call().entity(GradingUserAnswerAIResponse.class);


        answerEntity.setFeedback(aiResponse.getFeedback());


        return Math.min(aiResponse.getPoint(), maxPoints);
    }
}
