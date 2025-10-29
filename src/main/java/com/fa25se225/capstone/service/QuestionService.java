package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.QuestionCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionUpdateRequest;
import com.fa25se225.capstone.dto.response.QuestionResponse;

import java.util.List;

public interface QuestionService {
    
    QuestionResponse createQuestion(QuestionCreationRequest request);
    
    QuestionResponse getQuestionById(String id);
    
    PageResponse<List<QuestionResponse>> getAllQuestions(int pageNo, int pageSize, String... sorts);
    
    PageResponse<List<QuestionResponse>> getQuestionsByTeacher(String teacherId, int pageNo, int pageSize, String... sorts);
    
    QuestionResponse updateQuestion(String id, QuestionUpdateRequest request);
    
    void deleteQuestion(String id);
}
