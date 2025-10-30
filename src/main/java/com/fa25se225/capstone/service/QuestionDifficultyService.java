package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.QuestionDifficultyCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionDifficultyUpdateRequest;
import com.fa25se225.capstone.dto.response.QuestionDifficultyResponse;

import java.util.List;

public interface QuestionDifficultyService {
    
    QuestionDifficultyResponse createQuestionDifficulty(QuestionDifficultyCreationRequest request);
    
    QuestionDifficultyResponse getQuestionDifficultyById(String id);
    
    PageResponse<List<QuestionDifficultyResponse>> getAllQuestionDifficulties(int pageNo, int pageSize, String... sorts);
    
    QuestionDifficultyResponse updateQuestionDifficulty(String id, QuestionDifficultyUpdateRequest request);
    
    void deleteQuestionDifficulty(String id);
}
