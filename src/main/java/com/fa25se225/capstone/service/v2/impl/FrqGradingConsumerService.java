package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.kafka.FrqGradingEvent;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.GradingUserAnswerAIResponse;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.entity.v2.StudentAnswerV2;
import com.fa25se225.capstone.mapper.v2.ExamAttemptV2Mapper;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.StudentAnswerV2Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FrqGradingConsumerService {

    private final StudentAnswerV2Repository studentAnswerRepository;
    private final ExamAttemptV2Repository attemptRepository;
    private final ChatClient chatClient;

    private final SseNotificationService sseService;
    private final ExamAttemptV2Mapper attemptMapper;

    @KafkaListener(topics = "frq_grading_tasks", groupId = "frq-grading-group")
    @Transactional
    public void handleFrqGradingTask(FrqGradingEvent event) {
        log.info("Received FRQ grading task for studentAnswerId: {}", event.studentAnswerId());

        StudentAnswerV2 studentAnswer = studentAnswerRepository.findById(event.studentAnswerId())
                .orElse(null);

        if (studentAnswer == null) {
            log.error("Cannot find StudentAnswerV2 with id: {}. Task aborted.", event.studentAnswerId());
            return;
        }

        double score = gradeFrqWithAI(
                event.modelAnswer(),
                event.studentAnswerText(),
                event.maxPoints(),
                studentAnswer
        );

        studentAnswer.setScore(score);
        studentAnswerRepository.save(studentAnswer);

        checkIfAttemptIsFullyGraded(event.attemptId());
    }

    private double gradeFrqWithAI(String modelAnswer, String studentAnswerText, double maxPoints, StudentAnswerV2 answerEntity) {
        if (!StringUtils.hasText(studentAnswerText)) {
            return 0.0;
        }

        String systemPrompt = String.format(
                "You are an AI grading assistant. Grade the student's answer based on the model answer and the maximum points. " +
                        "The score must be a number between 0 and %.1f.\n\n" +
                        "--- MODEL ANSWER ---\n%s\n\n" +
                        "--- MAXIMUM POINTS: %.1f ---",
                maxPoints, modelAnswer, maxPoints
        );

        try {
            var aiResponse = chatClient.prompt().system(systemPrompt)
                    .user(studentAnswerText)
                    .call().entity(GradingUserAnswerAIResponse.class);

            answerEntity.setFeedback(aiResponse.getFeedback());
            return Math.min(aiResponse.getPoint(), maxPoints);
        } catch (Exception e) {
            log.error("Failed to grade FRQ with AI for studentAnswerId: {}. Error: {}", answerEntity.getId(), e.getMessage());
            answerEntity.setFeedback("Grading Error: Could not contact AI assistant.");
            return 0.0;
        }
    }

    private void checkIfAttemptIsFullyGraded(String attemptId) {
        ExamAttemptV2 attempt = attemptRepository.findById(attemptId)
                .orElse(null);
        if (attempt == null) return;

        List<StudentAnswerV2> allAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);

        boolean allGraded = allAnswers.stream()
                .noneMatch(sa -> Objects.isNull(sa.getScore()));

        if (allGraded) {
            log.info("All questions for attempt {} are graded. Calculating final score.", attemptId);

            double totalScore = allAnswers.stream()
                    .mapToDouble(StudentAnswerV2::getScore)
                    .sum();

            attempt.setScore(totalScore);
            attempt.setStatus(AttemptStatusV2.COMPLETED);
            ExamAttemptV2 savedAttempt = attemptRepository.save(attempt);

            ExamAttemptV2Response responseDTO = attemptMapper.toResponse(savedAttempt);
            sseService.sendGradingCompleteNotification(attemptId, responseDTO);

        } else {
            log.info("Attempt {} still has questions pending grading.", attemptId);
        }
    }
}