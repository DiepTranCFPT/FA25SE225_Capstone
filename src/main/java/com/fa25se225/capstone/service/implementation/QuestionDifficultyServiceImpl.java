package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.QuestionDifficultyCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionDifficultyUpdateRequest;
import com.fa25se225.capstone.dto.response.QuestionDifficultyResponse;
import com.fa25se225.capstone.entity.QuestionDifficulty;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.QuestionDifficultyMapper;
import com.fa25se225.capstone.repository.QuestionDifficultyRepository;
import com.fa25se225.capstone.service.QuestionDifficultyService;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionDifficultyServiceImpl implements QuestionDifficultyService {
    
    private final QuestionDifficultyRepository questionDifficultyRepository;
    private final QuestionDifficultyMapper questionDifficultyMapper;
    private final PageHelper pageHelper;
    
    @Override
    @Transactional
    public QuestionDifficultyResponse createQuestionDifficulty(QuestionDifficultyCreationRequest request) {
        log.info("Creating question difficulty with name: {}", request.name());
        
        QuestionDifficulty questionDifficulty = questionDifficultyMapper.toEntity(request);
        QuestionDifficulty savedDifficulty = questionDifficultyRepository.save(questionDifficulty);
        
        log.info("Successfully created question difficulty with id: {}", savedDifficulty.getId());
        return questionDifficultyMapper.toResponse(savedDifficulty);
    }
    
    @Override
    public QuestionDifficultyResponse getQuestionDifficultyById(String id) {
        log.info("Getting question difficulty by id: {}", id);
        
        QuestionDifficulty questionDifficulty = questionDifficultyRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_NOT_FOUND));
        
        return questionDifficultyMapper.toResponse(questionDifficulty);
    }
    
    @Override
    public PageResponse<List<QuestionDifficultyResponse>> getAllQuestionDifficulties(int pageNo, int pageSize, String... sorts) {
        log.info("Getting all question difficulties with pagination - page: {}, size: {}", pageNo, pageSize);
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<QuestionDifficulty> page = questionDifficultyRepository.findAllNotDeleted(pageable);
        
        List<QuestionDifficultyResponse> responses = page.getContent()
                .stream()
                .map(questionDifficultyMapper::toResponse)
                .toList();
        
        return PageResponse.<List<QuestionDifficultyResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    @Transactional
    public QuestionDifficultyResponse updateQuestionDifficulty(String id, QuestionDifficultyUpdateRequest request) {
        log.info("Updating question difficulty with id: {}", id);
        
        QuestionDifficulty questionDifficulty = questionDifficultyRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_NOT_FOUND));
        
        questionDifficultyMapper.updateEntity(questionDifficulty, request);
        QuestionDifficulty updatedDifficulty = questionDifficultyRepository.save(questionDifficulty);
        
        log.info("Successfully updated question difficulty with id: {}", updatedDifficulty.getId());
        return questionDifficultyMapper.toResponse(updatedDifficulty);
    }
    
    @Override
    @Transactional
    public void deleteQuestionDifficulty(String id) {
        log.info("Deleting question difficulty with id: {}", id);
        
        QuestionDifficulty questionDifficulty = questionDifficultyRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_NOT_FOUND));
        
        questionDifficulty.setDeleted(true);
        questionDifficultyRepository.save(questionDifficulty);
        
        log.info("Successfully deleted question difficulty with id: {}", id);
    }
}
