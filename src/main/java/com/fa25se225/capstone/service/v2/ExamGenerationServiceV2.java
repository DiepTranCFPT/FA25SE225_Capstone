package com.fa25se225.capstone.service.v2;

import com.fa25se225.capstone.dto.v2.ExamV2Response;
import com.fa25se225.capstone.dto.v2.SubjectV2Response;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.ExamAnswerV2Mapper;
import com.fa25se225.capstone.mapper.v2.ExamV2Mapper;
import com.fa25se225.capstone.repository.v2.*;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamGenerationServiceV2 {

    private final ExamTemplateV2Repository templateRepository;
    private final QuestionV2Repository questionRepository;
    private final ExamV2Repository examRepository;
    private final ExamAttemptV2Repository attemptRepository;
    private final AccountUtil accountUtil;
    private final ExamV2Mapper examV2Mapper;
    private final ExamAnswerV2Mapper examAnswerV2Mapper;

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
                    rule.getDifficulty().getName(),
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
}