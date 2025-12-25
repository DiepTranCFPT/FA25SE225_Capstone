package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.constant.QuestionType;
import com.fa25se225.capstone.dto.v2.request.AnswerV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionCreationV2Request;
import com.fa25se225.capstone.dto.v2.request.QuestionContextRequest;
import com.fa25se225.capstone.dto.v2.request.QuestionImportRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionImportResponse;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import com.fa25se225.capstone.entity.v2.QuestionDifficultyV2;
import com.fa25se225.capstone.entity.v2.QuestionTopicV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import com.fa25se225.capstone.entity.v2.QuestionContextV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.v2.AnswerV2Mapper;
import com.fa25se225.capstone.mapper.v2.QuestionV2Mapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.v2.QuestionDifficultyV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionTopicV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.repository.v2.QuestionContextV2Repository;
import com.fa25se225.capstone.service.implementation.FileUploadService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionImportServiceImpl implements QuestionImportService {

    private final QuestionV2Repository questionV2Repository;
    private final SubjectRepository subjectRepository;
    private final QuestionDifficultyV2Repository questionDifficultyV2Repository;
    private final QuestionTopicV2Repository questionTopicV2Repository;
    private final QuestionContextV2Repository questionContextV2Repository;
    private final QuestionV2Mapper questionV2Mapper;
    private final AnswerV2Mapper answerV2Mapper;
    private final AccountUtil accountUtil;
    private final FileUploadService fileUploadService;

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

        // Cache map: key = contextTitle, value = QuestionContextV2
        Map<String, QuestionContextV2> contextCache = new HashMap<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalProcessed++;

                try {
                    QuestionCreationV2Request questionRequest = parseRowToQuestionRequest(row, request.getSubjectId());

                    // Handle context if exists
                    QuestionContextV2 context = null;
                    if (questionRequest.getContext() != null) {
                        String contextTitle = questionRequest.getContext().getTitle();

                        // Check in cache first
                        if (contextCache.containsKey(contextTitle)) {
                            context = contextCache.get(contextTitle);
                            log.debug("Using cached context: {}", contextTitle);
                        } else {
                            // Try to find in DB by title and current user
                            context = findOrCreateContext(questionRequest.getContext(), subject, currentUser);
                            contextCache.put(contextTitle, context);
                            log.debug("Context cached: {} with ID: {}", contextTitle, context.getId());
                        }
                    }

                    QuestionV2 savedQuestion = createQuestionFromRequest(questionRequest, subject, currentUser, context);
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

        log.info("Import completed. Total: {}, Success: {}, Errors: {}", totalProcessed, successQuestionIds.size(), errorMessages.size());

        return QuestionImportResponse.builder()
                .totalProcessed(totalProcessed)
                .successCount(successQuestionIds.size())
                .errorCount(errorMessages.size())
                .errorMessages(errorMessages)
                .successQuestionIds(successQuestionIds)
                .build();
    }

    private QuestionCreationV2Request parseRowToQuestionRequest(Row row, String subjectId) {
        // Expected columns:
        // Content, Type, DifficultyName, TopicName,
        // Answer1, IsCorrect1, Answer2, IsCorrect2, Answer3, IsCorrect3, Answer4, IsCorrect4,
        // ContextTitle, ContextContent, ContextImageUrl, ContextAudioUrl

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
                        .explanation(null)
                        .build());
            }
        }

        if (answers.isEmpty()) {
            throw new RuntimeException("At least one answer is required");
        }

        // Parse context fields (optional) - 4 columns at the end
        String contextTitle = getCellValueAsString(row, 12);
        String contextContent = getCellValueAsString(row, 13);
        String contextImageUrl = getCellValueAsString(row, 14);
        String contextAudioUrl = getCellValueAsString(row, 15);

        // Build QuestionContextRequest if context title exists
        QuestionContextRequest contextRequest = null;
        if (contextTitle != null && !contextTitle.trim().isEmpty()) {
            if (contextContent == null || contextContent.trim().isEmpty()) {
                throw new RuntimeException("Context content is required when context title is provided");
            }

            contextRequest = new QuestionContextRequest(
                    contextTitle.trim(),
                    contextContent.trim(),
                    contextImageUrl != null && !contextImageUrl.trim().isEmpty() ? contextImageUrl.trim() : null,
                    contextAudioUrl != null && !contextAudioUrl.trim().isEmpty() ? contextAudioUrl.trim() : null,
                    subjectId
            );
        }

        return QuestionCreationV2Request.builder()
                .context(contextRequest)
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

    /**
     * Find existing context in DB by title and user, or create new one
     */
    private QuestionContextV2 findOrCreateContext(QuestionContextRequest contextRequest, Subject subject, User currentUser) {
        // Try to find existing context by title and current user
        List<QuestionContextV2> existingContexts = questionContextV2Repository
                .findByCreatedByIdAndSubjectId(currentUser.getId(), subject.getId());

        // Find by title match
        QuestionContextV2 existingContext = existingContexts.stream()
                .filter(ctx -> ctx.getTitle().equals(contextRequest.getTitle()))
                .findFirst()
                .orElse(null);

        if (existingContext != null) {
            log.debug("Found existing context in DB: {} (ID: {})", contextRequest.getTitle(), existingContext.getId());
            return existingContext;
        }

        // Create new context
        QuestionContextV2 newContext = QuestionContextV2.builder()
                .title(contextRequest.getTitle())
                .content(contextRequest.getContent())
                .imageUrl(contextRequest.getImageUrl())
                .audioUrl(contextRequest.getAudioUrl())
                .subject(subject)
                .createdBy(currentUser)
                .build();

        QuestionContextV2 savedContext = questionContextV2Repository.save(newContext);
        
        // Confirm file usage for context images/audio
        if (savedContext.getImageUrl() != null) {
            fileUploadService.confirmFileUsage(savedContext.getImageUrl());
        }
        if (savedContext.getAudioUrl() != null) {
            fileUploadService.confirmFileUsage(savedContext.getAudioUrl());
        }
        
        log.debug("Created new context: {} (ID: {})", contextRequest.getTitle(), savedContext.getId());

        return savedContext;
    }

    private QuestionV2 createQuestionFromRequest(QuestionCreationV2Request request, Subject subject, User currentUser, QuestionContextV2 context) {
        // Validate question type
        try {
            QuestionType.fromValue(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid question type: " + request.getType());
        }

        // Find difficulty
        QuestionDifficultyV2 difficulty = questionDifficultyV2Repository.findByNameIgnoreCase(request.getDifficultyName())
                .orElseThrow(() -> new RuntimeException("Difficulty not found: " + request.getDifficultyName()));

        // Find topic
        QuestionTopicV2 topic = questionTopicV2Repository.findByNameIgnoreCase(request.getTopicName())
                .orElseThrow(() -> new RuntimeException("Topic not found: " + request.getTopicName()));

        // Validate answers
        validateQuestionAnswers(request.getAnswers(), request.getType());

        QuestionV2 question = questionV2Mapper.toEntity(request);
        question.setCreatedBy(currentUser);
        question.setSubject(subject);
        question.setDifficulty(difficulty);
        question.setTopic(topic);

        // Link to context if provided
        if (context != null) {
            question.setContext(context);
        }
        
        // Confirm file usage for question images/audio
        if (question.getImageUrl() != null) {
            fileUploadService.confirmFileUsage(question.getImageUrl());
        }
        if (question.getAudioUrl() != null) {
            fileUploadService.confirmFileUsage(question.getAudioUrl());
        }

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
                "Answer3", "IsCorrect3", "Answer4", "IsCorrect4",
                "ContextTitle", "ContextContent", "ContextImageUrl", "ContextAudioUrl"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Example 1: Question with context (IELTS Reading)
            Row exampleRow1 = sheet.createRow(1);
            exampleRow1.createCell(0).setCellValue("What is the main topic of the passage?");
            exampleRow1.createCell(1).setCellValue("MCQ");
            exampleRow1.createCell(2).setCellValue("Medium");
            exampleRow1.createCell(3).setCellValue("Reading Comprehension");
            exampleRow1.createCell(4).setCellValue("Climate change");
            exampleRow1.createCell(5).setCellValue("true");
            exampleRow1.createCell(6).setCellValue("Weather patterns");
            exampleRow1.createCell(7).setCellValue("false");
            exampleRow1.createCell(8).setCellValue("Global warming");
            exampleRow1.createCell(9).setCellValue("false");
            exampleRow1.createCell(10).setCellValue("Temperature");
            exampleRow1.createCell(11).setCellValue("false");
            exampleRow1.createCell(12).setCellValue("IELTS Reading Passage - Climate Change");
            exampleRow1.createCell(13).setCellValue("Climate change is one of the most pressing issues of our time. Scientists worldwide have documented rising temperatures and changing weather patterns...");
            exampleRow1.createCell(14).setCellValue(""); // ContextImageUrl
            exampleRow1.createCell(15).setCellValue(""); // ContextAudioUrl

            // Example 2: Another question in the same context
            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("According to the passage, what have scientists documented?");
            exampleRow2.createCell(1).setCellValue("MCQ");
            exampleRow2.createCell(2).setCellValue("Medium");
            exampleRow2.createCell(3).setCellValue("Reading Comprehension");
            exampleRow2.createCell(4).setCellValue("Rising temperatures");
            exampleRow2.createCell(5).setCellValue("true");
            exampleRow2.createCell(6).setCellValue("Falling temperatures");
            exampleRow2.createCell(7).setCellValue("false");
            exampleRow2.createCell(8).setCellValue("Climate change");
            exampleRow2.createCell(9).setCellValue("false");
            exampleRow2.createCell(10).setCellValue("Human");
            exampleRow2.createCell(11).setCellValue("false");
            exampleRow2.createCell(12).setCellValue("IELTS Reading Passage - Climate Change"); // Same context title
            exampleRow2.createCell(13).setCellValue("Climate change is one of the most pressing issues of our time. Scientists worldwide have documented rising temperatures and changing weather patterns...");
            exampleRow2.createCell(14).setCellValue("");
            exampleRow2.createCell(15).setCellValue("");

            // Example 3: Standalone question without context
            Row exampleRow3 = sheet.createRow(3);
            exampleRow3.createCell(0).setCellValue("What is 2 + 2?");
            exampleRow3.createCell(1).setCellValue("MCQ");
            exampleRow3.createCell(2).setCellValue("Easy");
            exampleRow3.createCell(3).setCellValue("Basic Math");
            exampleRow3.createCell(4).setCellValue("3");
            exampleRow3.createCell(5).setCellValue("false");
            exampleRow3.createCell(6).setCellValue("4");
            exampleRow3.createCell(7).setCellValue("true");
            exampleRow3.createCell(8).setCellValue("5");
            exampleRow3.createCell(9).setCellValue("false");
            exampleRow3.createCell(10).setCellValue("6");
            exampleRow3.createCell(11).setCellValue("false");
            exampleRow3.createCell(12).setCellValue(""); // No context
            exampleRow3.createCell(13).setCellValue("");
            exampleRow3.createCell(14).setCellValue("");
            exampleRow3.createCell(15).setCellValue("");

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
