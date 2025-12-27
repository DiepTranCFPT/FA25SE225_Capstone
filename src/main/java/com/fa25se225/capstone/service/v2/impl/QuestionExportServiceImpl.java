package com.fa25se225.capstone.service.v2.impl;

import com.fa25se225.capstone.dto.v2.request.QuestionExportRequest;
import com.fa25se225.capstone.dto.v2.response.QuestionTopicV2Response;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AnswerV2;
import com.fa25se225.capstone.entity.v2.QuestionContextV2;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.v2.QuestionExportService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionExportServiceImpl implements QuestionExportService {

    private final QuestionV2Repository questionV2Repository;
    private final SubjectRepository subjectRepository;
    private final AccountUtil accountUtil;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportQuestionsToExcel(QuestionExportRequest request) {
        
        User currentUser = accountUtil.getCurrentUser();


        log.info("Exporting questions to Excel for subject: {}", request.getSubjectId());

        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        // Fetch questions
        List<QuestionV2> questions = questionV2Repository.findAllById(request.getQuestionIds());
        
        // Filter questions by subject to ensure they belong to the requested subject
        List<QuestionV2> filteredQuestions = questions.stream()
                .filter(q -> q.getSubject().getId().equals(subject.getId()))
                .filter(q -> q.getCreatedBy().getId().equals(currentUser.getId()))
                .sorted(Comparator.comparing(
                        (QuestionV2 q) -> q.getTopic() == null ? null : q.getTopic().getName(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();

        if (filteredQuestions.isEmpty()) {
            throw new AppException(ErrorCode.QUESTION_V2_NOT_FOUND);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions Export");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Content", "Type", "DifficultyName", "TopicName",
                "Answer1", "IsCorrect1", "Answer2", "IsCorrect2",
                "Answer3", "IsCorrect3", "Answer4", "IsCorrect4", "Answer5", "IsCorrect5",
                "ContextTitle", "ContextContent", "ContextImageUrl", "ContextAudioUrl",
                "QuestionImageUrl", "QuestionAudioUrl"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (QuestionV2 question : filteredQuestions) {
                Row row = sheet.createRow(rowNum++);

                // Basic question info
                row.createCell(0).setCellValue(question.getContent());
                row.createCell(1).setCellValue(question.getType().getValue());
                row.createCell(2).setCellValue(question.getDifficulty().getName());
                row.createCell(3).setCellValue(question.getTopic().getName());

                // Answers
                List<AnswerV2> answers = question.getAnswers();
                for (int i = 0; i < 5; i++) {
                    if (i < answers.size()) {
                        AnswerV2 answer = answers.get(i);
                        row.createCell(4 + (i * 2)).setCellValue(answer.getContent());
                        row.createCell(5 + (i * 2)).setCellValue(String.valueOf(answer.getIsCorrect()));
                    } else {
                        row.createCell(4 + (i * 2)).setCellValue("");
                        row.createCell(5 + (i * 2)).setCellValue("false");
                    }
                }

                // Context info
                QuestionContextV2 context = question.getContext();
                if (context != null) {
                    row.createCell(14).setCellValue(context.getTitle());
                    row.createCell(15).setCellValue(context.getContent());
                    row.createCell(16).setCellValue(context.getImageUrl() != null ? context.getImageUrl() : "");
                    row.createCell(17).setCellValue(context.getAudioUrl() != null ? context.getAudioUrl() : "");
                } else {
                    row.createCell(14).setCellValue("");
                    row.createCell(15).setCellValue("");
                    row.createCell(16).setCellValue("");
                    row.createCell(17).setCellValue("");
                }

                // Question media
                row.createCell(18).setCellValue(question.getImageUrl() != null ? question.getImageUrl() : "");
                row.createCell(19).setCellValue(question.getAudioUrl() != null ? question.getAudioUrl() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting questions to Excel", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
