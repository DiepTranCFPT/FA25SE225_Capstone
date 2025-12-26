package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.kafka.FrqGradingEvent;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.response.GradingUserAnswerAIResponse;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.entity.v2.ExamQuestionV2;
import com.fa25se225.capstone.entity.v2.StudentAnswerV2;
import com.fa25se225.capstone.mapper.v2.ExamAttemptV2Mapper;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.ExamQuestionV2Repository;
import com.fa25se225.capstone.repository.v2.StudentAnswerV2Repository;
import com.fa25se225.capstone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FrqGradingConsumerService {

    private final StudentAnswerV2Repository studentAnswerRepository;
    private final ExamAttemptV2Repository attemptRepository;

    @NonFinal
    @Autowired
    @Qualifier("chatClientWithoutChatMemory")
    private ChatClient chatClient;

    private final SseNotificationService sseService;
    private final ExamAttemptV2Mapper attemptMapper;

    private final ExamQuestionV2Repository examQuestionRepository;
    private final NotificationService notificationService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            include = {RuntimeException.class}
    )
    @KafkaListener(topics = "frq_grading_tasks", groupId = "frq-grading-group")
    @Transactional
    public void handleFrqGradingTask(FrqGradingEvent event) {
        log.info("Received FRQ grading task for studentAnswerId: {}", event.studentAnswerId());

        StudentAnswerV2 studentAnswer = studentAnswerRepository.findById(event.studentAnswerId())
                .orElse(null);

        if (studentAnswer == null) {
            log.warn("StudentAnswerV2 not found id: {}. Retrying...", event.studentAnswerId());
            throw new RuntimeException("StudentAnswer not found, retrying...");
        }

        double score = gradeFrqWithAI(
                event.modelAnswer(),
                event.studentAnswerText(),
                event.maxPoints(),
                event.questionContent(),
                event.contextTitle(),
                event.contextContent(),
                studentAnswer
        );

        studentAnswer.setScore(score);
        studentAnswerRepository.save(studentAnswer);

        checkIfAttemptIsFullyGraded(event.attemptId());
    }

    private double gradeFrqWithAI(String modelAnswer,
                                  String studentAnswerText,
                                  double maxPoints,
                                  String questionContent,
                                  String contextTitle,
                                  String contextContent,
                                  StudentAnswerV2 answerEntity) {

        if (!StringUtils.hasText(studentAnswerText)) {
            return 0.0;
        }

        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("You are a strict and impartial examiner grading an Advanced Placement (AP) exam.\n");
        promptBuilder.append("Your goal is to grade the STUDENT ANSWER based on the provided CONTEXT, QUESTION, and RUBRIC (MODEL ANSWER).\n\n");

        if (StringUtils.hasText(contextContent)) {
            promptBuilder.append("--- CONTEXT / PASSAGE ---\n");
            if (StringUtils.hasText(contextTitle)) {
                promptBuilder.append("Title: ").append(contextTitle).append("\n");
            }
            promptBuilder.append(contextContent).append("\n\n");
        }

        promptBuilder.append(questionContent != null ? questionContent : "No question content provided").append("\n\n");

        promptBuilder.append("--- MODEL ANSWER (RUBRIC) ---\n");
        promptBuilder.append(modelAnswer).append("\n\n");

        promptBuilder.append(String.format("""
             --- GRADING CONFIGURATION ---
             MAXIMUM POINTS: %.1f
             
             --- GRADING RULES ---
             1. **Context Adherence**: If a Context is provided, the answer MUST reference or be consistent with it.
             2. **Relevance**: If the answer is off-topic, admits ignorance, or is random text -> SCORE 0.
             3. **Accuracy**: Compare strictly against the MODEL ANSWER. Do not award points for fluff.
             4. **Partial Credit**: Award partial points for partial correctness based on the logic of the Model Answer.
             5. **Output**: Return strictly JSON: {"point": number, "feedback": "string"}.
             6. **Formatting**: If using LaTeX math (like \\pi, \\frac), you MUST double-escape backslashes (e.g., \\\\pi, \\\\frac) so the JSON remains valid.
             
             Now, grade the STUDENT ANSWER provided below.
             """, maxPoints));

        try {
            var aiResponse = chatClient.prompt()
                    .system(promptBuilder.toString())
                    .user(studentAnswerText)
                    .call()
                    .entity(GradingUserAnswerAIResponse.class);

            answerEntity.setFeedback(aiResponse.getFeedback());

            return Math.min(aiResponse.getPoint(), maxPoints);

        } catch (Exception e) {
            log.error("AI Grading failed for answer {}: {}", answerEntity.getId(), e.getMessage());
            throw new RuntimeException("AI Service Unavailable: " + e.getMessage());
        }
    }

    @DltHandler
    @Transactional
    public void handleDlt(FrqGradingEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Grading failed for studentAnswerId: {} after retries. Moving to Manual Review.", event.studentAnswerId());

        StudentAnswerV2 studentAnswer = studentAnswerRepository.findById(event.studentAnswerId()).orElse(null);
        if (studentAnswer != null) {
            studentAnswer.setFeedback("The AI system crashed after several tries. Teachers, please grade manually.");
            studentAnswerRepository.save(studentAnswer);

        }
    }

    private void checkIfAttemptIsFullyGraded(String attemptId) {
        ExamAttemptV2 attempt = attemptRepository.findByIdWithLock(attemptId).orElse(null);
        if (attempt == null) return;

        List<StudentAnswerV2> studentAnswers = studentAnswerRepository.findByExamAttemptIdWithDetails(attemptId);

        boolean anyPending = studentAnswers.stream().anyMatch(sa -> Objects.isNull(sa.getScore()));

        if (!anyPending) {
            log.info("Grading tasks completed for attempt {}. Finalizing...", attemptId);

            List<ExamQuestionV2> allQuestions = examQuestionRepository.findAllByExamIdWithDetails(attempt.getExam().getId());

            Map<String, StudentAnswerV2> answerMap = studentAnswers.stream()
                    .collect(Collectors.toMap(sa -> sa.getExamQuestion().getId(), Function.identity()));

            double totalScore = 0.0;
            for (StudentAnswerV2 sa : studentAnswers) {
                if (sa.getScore() != null) {
                    totalScore += sa.getScore();
                }
            }
            attempt.setScore(totalScore);

            String studentName = attempt.getUser().getFirstName() + " " + attempt.getUser().getLastName();
            String aiComment = generateOverallCommentWithAI(allQuestions, answerMap, studentName);
            attempt.setComment(aiComment);

            attempt.setStatus(AttemptStatusV2.COMPLETED);
            ExamAttemptV2 savedAttempt = attemptRepository.save(attempt);

            ExamAttemptV2Response responseDTO = attemptMapper.toResponse(savedAttempt);
            sseService.sendGradingCompleteNotification(attemptId, responseDTO);
            String message = "Your exam attempt has been graded successfully.";
            notificationService.sendNotify(savedAttempt.getUser().getEmail(), message, savedAttempt.getExam().getTitle());

        }
    }



    private String generateOverallCommentWithAI(List<ExamQuestionV2> allQuestions, Map<String, StudentAnswerV2> answerMap, String studentName) {
        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("Detailed results list:\n");

        int correctCount = 0;
        int skippedCount = 0;

        for (ExamQuestionV2 examQuestion : allQuestions) {
            StudentAnswerV2 studentAns = answerMap.get(examQuestion.getId());

            String topic = examQuestion.getQuestion().getTopic().getName();
            String difficulty = examQuestion.getQuestion().getDifficulty().getName();
            double maxPoints = examQuestion.getPoints();

            String status;
            double scored;

            if (Objects.isNull(studentAns)) {
                status = "SKIP (DO NOT DO)";
                scored = 0.0;
                skippedCount++;
            } else {
                scored = studentAns.getScore() != null ? studentAns.getScore() : 0.0;
                if (scored >= maxPoints) {
                    status = "CORRECT";
                    correctCount++;
                } else if (scored > 0) {
                    status = "PARTLY CORRECT";
                } else {
                    status = "WRONG";
                }
            }

            summaryBuilder.append(String.format("- Topic: %s | Difficulty: %s | Result: %s (%.1f/%.1f score(s))\n",
                    topic, difficulty, status, scored, maxPoints));
        }

        summaryBuilder.append(String.format("\nSummary: Total number of questions: %d. Number of questions skipped: %d. Number of correct questions: %d.",
                allQuestions.size(), skippedCount, correctCount));

        String prompt = String.format(
                """
                You are a professional and caring AP (Advanced Placement) academic advisor.
                Based on the student's detailed test results below, write a summary review (about 100-150 words) to comment on student's work..
                Student Name: %s
                Content should include:
                1. Strengths: What knowledge does the student have mastered?
                2. Weaknesses: Where is the student having difficulty (what topic, what type of test)?
                3. Advice: What parts do you need to review to improve your score?
                4. Tone: Positive, encouraging but straightforward.
                (Caution** If a student SKIPS a lot of questions, remind them about time management or confidence, don't just praise the ones they get right.
                                   Distinguish between 'Doing it wrong' (gaps in knowledge) and 'Skipping' (maybe because they didn't make it on time).)
                %s""", studentName, summaryBuilder
        );

        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("Failed to generate AI comment", e);
            return "The test has been graded. Please review each question in detail to learn from your experience.";
        }
    }
}