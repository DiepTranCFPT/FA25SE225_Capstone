package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.v2.request.AnswerV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionImportRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionImportResponse;
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
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.v2.QuestionImportService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionImportServiceImpl implements QuestionImportService {

    private final QuestionV2Repository questionV2Repository;
    private final SubjectRepository subjectRepository;
    private final QuestionDifficultyV2Repository questionDifficultyV2Repository;
    private final QuestionTopicV2Repository questionTopicV2Repository;
    private final QuestionV2Mapper questionV2Mapper;
    private final AnswerV2Mapper answerV2Mapper;
    private final AccountUtil accountUtil;

    @Override
    @Transactional
    public QuestionImportResponse importQuestionsFromExcel(MultipartFile file, QuestionImportRequest request) {
        log.info("Starting import questions from Excel file for subject: {}", request.getSubjectId());

        if (file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        User currentUser = accountUtil.getCurrentUser();

        List<String> errorMessages = new ArrayList<>();
        List<String> successQuestionIds = new ArrayList<>();
        int totalProcessed = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalProcessed++;

                try {
                    QuestionCreationV2Request questionRequest = parseRowToQuestionRequest(row, request.getSubjectId());
                    QuestionV2 savedQuestion = createQuestionFromRequest(questionRequest, subject, currentUser);
                    successQuestionIds.add(savedQuestion.getId());
                    log.debug("Successfully imported question at row {}: {}", i + 1, savedQuestion.getId());
                } catch (Exception e) {
                    String errorMsg = String.format("Row %d: %s", i + 1, e.getMessage());
                    errorMessages.add(errorMsg);
                    log.warn("Error importing question at row {}: {}", i + 1, e.getMessage());

                    if (!request.isSkipErrors()) {
                        throw new AppException(ErrorCode.VALIDATION_ERROR);
                    }
                }
            }

        } catch (IOException e) {
            log.error("Error reading Excel file", e);
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        return QuestionImportResponse.builder()
                .totalProcessed(totalProcessed)
                .successCount(successQuestionIds.size())
                .errorCount(errorMessages.size())
                .errorMessages(errorMessages)
                .successQuestionIds(successQuestionIds)
                .build();
    }

    private QuestionCreationV2Request parseRowToQuestionRequest(Row row, String subjectId) {
        // Expected columns: Content, Type, DifficultyName, TopicName, Answer1, IsCorrect1, Answer2, IsCorrect2, Answer3, IsCorrect3, Answer4, IsCorrect4, Explanation

        String content = getCellValueAsString(row, 0);
        String type = getCellValueAsString(row, 1);
        String difficultyName = getCellValueAsString(row, 2);
        String topicName = getCellValueAsString(row, 3);

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Content is required");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new RuntimeException("Question type is required");
        }
        if (difficultyName == null || difficultyName.trim().isEmpty()) {
            throw new RuntimeException("Difficulty name is required");
        }
        if (topicName == null || topicName.trim().isEmpty()) {
            throw new RuntimeException("Topic name is required");
        }

        // Parse answers (up to 4 answers)
        List<AnswerV2Request> answers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String answerContent = getCellValueAsString(row, 4 + (i * 2));
            String isCorrectStr = getCellValueAsString(row, 5 + (i * 2));

            if (answerContent != null && !answerContent.trim().isEmpty()) {
                boolean isCorrect = "true".equalsIgnoreCase(isCorrectStr) || "1".equals(isCorrectStr);
                answers.add(AnswerV2Request.builder()
                        .content(answerContent.trim())
                        .isCorrect(isCorrect)
                        .explanation(null) // Can add explanation parsing if needed
                        .build());
            }
        }

        if (answers.isEmpty()) {
            throw new RuntimeException("At least one answer is required");
        }

        return QuestionCreationV2Request.builder()
                .content(content.trim())
                .type(type.trim())
                .subjectId(subjectId)
                .difficultyName(difficultyName.trim())
                .topicName(topicName.trim())
                .answers(answers)
                .build();
    }

    private String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private QuestionV2 createQuestionFromRequest(QuestionCreationV2Request request, Subject subject, User currentUser) {
        // Validate question type
        try {
            QuestionType.fromValue(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid question type: " + request.getType());
        }

        // Find or create difficulty
        QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                .orElseThrow(() -> new RuntimeException("Difficulty not found: " + request.getDifficultyName()));

        // Find or create topic
        QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                .orElseThrow(() -> new RuntimeException("Topic not found: " + request.getTopicName()));

        // Validate answers
        validateQuestionAnswers(request.getAnswers(), request.getType());

        QuestionV2 question = questionV2Mapper.toEntity(request);
        question.setCreatedBy(currentUser);
        question.setSubject(subject);
        question.setDifficulty(difficulty);
        question.setTopic(topic);

        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            List<AnswerV2> answers = request.getAnswers().stream()
                    .map(answerRequest -> {
                        AnswerV2 answer = answerV2Mapper.toEntity(answerRequest);
                        answer.setQuestion(question);
                        return answer;
                    })
                    .collect(Collectors.toList());
            question.setAnswers(answers);
        }

        return questionV2Repository.save(question);
    }

    private void validateQuestionAnswers(List<AnswerV2Request> answers, String type) {
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("At least one answer is required");
        }

        List<AnswerV2Request> correctAnswers = answers.stream()
                .filter(AnswerV2Request::getIsCorrect)
                .toList();

        if (correctAnswers.isEmpty()) {
            throw new RuntimeException("At least one correct answer is required");
        }

        if (type.equalsIgnoreCase(QuestionType.FRQ.getValue()) && correctAnswers.size() > 1) {
            throw new RuntimeException("FRQ questions can only have one correct answer");
        }
    }

    @Override
    public byte[] generateExampleTemplate() {
        log.info("Generating Excel template for question import");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions Template");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Content", "Type", "DifficultyName", "TopicName",
                "Answer1", "IsCorrect1", "Answer2", "IsCorrect2",
                "Answer3", "IsCorrect3", "Answer4", "IsCorrect4"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create example rows
            Row exampleRow1 = sheet.createRow(1);
            exampleRow1.createCell(0).setCellValue("What is 2 + 2?");
            exampleRow1.createCell(1).setCellValue("MCQ");
            exampleRow1.createCell(2).setCellValue("Easy");
            exampleRow1.createCell(3).setCellValue("Basic Math");
            exampleRow1.createCell(4).setCellValue("3");
            exampleRow1.createCell(5).setCellValue("false");
            exampleRow1.createCell(6).setCellValue("4");
            exampleRow1.createCell(7).setCellValue("true");
            exampleRow1.createCell(8).setCellValue("5");
            exampleRow1.createCell(9).setCellValue("false");
            exampleRow1.createCell(10).setCellValue("6");
            exampleRow1.createCell(11).setCellValue("false");

            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("Explain the concept of gravity");
            exampleRow2.createCell(1).setCellValue("FRQ");
            exampleRow2.createCell(2).setCellValue("Medium");
            exampleRow2.createCell(3).setCellValue("Physics");
            exampleRow2.createCell(4).setCellValue("Gravity is a fundamental force");
            exampleRow2.createCell(5).setCellValue("true");

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel template", e);
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
