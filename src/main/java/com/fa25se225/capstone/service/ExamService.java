package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.ExamCreationRequest;
import com.fa25se225.capstone.dto.response.ExamResponse;
import java.util.List;

public interface ExamService {
    ExamResponse createExam(ExamCreationRequest exam);

    ExamResponse getExamById(String id);

    List<ExamResponse> getAllExams();

    List<ExamResponse> getAllExamsByUser();

    void deleteExam(String id);
}
