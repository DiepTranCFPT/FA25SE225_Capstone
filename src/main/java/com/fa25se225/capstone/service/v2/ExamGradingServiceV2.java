package com.fa25se225.capstone.service.v2;
import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.v2.ExamAttemptV2Response;
import com.fa25se225.capstone.dto.v2.StudentAnswerV2Request;
import com.fa25se225.capstone.dto.v2.SubmitAttemptV2Request;
import com.fa25se225.capstone.entity.v2.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.v2.AnswerV2Repository;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.repository.v2.ExamQuestionV2Repository;
import com.fa25se225.capstone.repository.v2.StudentAnswerV2Repository;
import com.fa25se225.capstone.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamGradingServiceV2 {

    private final ExamAttemptV2Repository attemptRepository;
    private final StudentAnswerV2Repository studentAnswerRepository;
    private final ExamQuestionV2Repository examQuestionRepository;
    private final AnswerV2Repository answerRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

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

    @Transactional
    public Double gradeExamAttempt2(String attemptId, SubmitAttemptV2Request request) {
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
        attemptRepository.save(attempt);
        return attempt.getScore();
    }

    private double gradeFrqWithAI(QuestionV2 question, String studentAnswerText, double maxPoints, StudentAnswerV2 answerEntity) {
//        if (studentAnswerText == null || studentAnswerText.isBlank()) {
//            return 0.0;
//        }
//
//
//        AnswerV2 modelAnswer = question.getAnswers().stream()
//                .filter(AnswerV2::getIsCorrect)
//                .findFirst()
//                .orElse(null);
//
//        if (modelAnswer == null) {
//            log.error("Không tìm thấy đáp án mẫu (model answer) cho câu hỏi FRQ ID: {}", question.getId());
//            return 0.0; // Không có đáp án mẫu, không chấm được
//        }
//
//        // Tạo prompt cho AI
//        String prompt = String.format(
//                "You are an AI grading assistant. Grade the student's answer based on the model answer and the maximum points. " +
//                        "Respond ONLY with a JSON object in the format: {\"score\": <number>, \"feedback\": \"<your_reasoning>\"}. " +
//                        "The score must be a number between 0 and %.1f.\n\n" +
//                        "--- MODEL ANSWER ---\n%s\n\n" +
//                        "--- STUDENT'S ANSWER ---\n%s\n\n" +
//                        "--- MAXIMUM POINTS: %.1f ---",
//                maxPoints, modelAnswer.getContent(), studentAnswerText, maxPoints
//        );
//
//        try {
//            // Gọi GeminiService của bạn
//            String aiResponse = geminiService.generateContent(prompt);
//
//            // Parse JSON response
//            Map<String, Object> responseMap = objectMapper.readValue(aiResponse, Map.class);
//            double score = ((Number) responseMap.get("score")).doubleValue();
//            String feedback = (String) responseMap.get("feedback");
//
//            answerEntity.setFeedback(feedback); // Lưu feedback
//
//            // Đảm bảo điểm AI không vượt quá maxPoints
//            return Math.min(score, maxPoints);
//
//        } catch (Exception e) {
//            log.error("Lỗi khi chấm điểm bằng AI cho câu hỏi {}: {}", question.getId(), e.getMessage());
//            answerEntity.setFeedback("Error during AI grading: " + e.getMessage());
//            return 0.0; // Lỗi -> 0 điểm
//        }
        return 2.0;
    }
}