package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.QuestionCreationRequest;
import com.fa25se225.capstone.dto.request.QuestionUpdateRequest;
import com.fa25se225.capstone.dto.response.QuestionResponse;
import com.fa25se225.capstone.entity.Question;
import com.fa25se225.capstone.entity.QuestionDifficulty;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.QuestionMapper;
import com.fa25se225.capstone.repository.QuestionDifficultyRepository;
import com.fa25se225.capstone.repository.QuestionRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.QuestionService;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    
    private final QuestionRepository questionRepository;
    private final QuestionDifficultyRepository questionDifficultyRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final PageHelper pageHelper;
    
    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionCreationRequest request) {
        log.info("Creating question with content: {}", request.content());
        
        log.debug("Getting current user for question creation");
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        log.debug("Validating and getting difficulty with id: {}", request.difficultyId());
        QuestionDifficulty difficulty = questionDifficultyRepository.findByIdNotDeleted(request.difficultyId())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_NOT_FOUND));
        
        log.debug("Creating question entity from request");
        Question question = questionMapper.toEntity(request);
        question.setCreatedBy(currentUser);
        question.setDifficulty(difficulty);
        
        Question savedQuestion = questionRepository.save(question);
        log.info("Successfully created question with id: {}", savedQuestion.getId());
        
        return questionMapper.toResponse(savedQuestion);
    }
    
    @Override
    public QuestionResponse getQuestionById(String id) {
        log.info("Getting question by id: {}", id);
        
        Question question = questionRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        
        return questionMapper.toResponse(question);
    }
    
    @Override
    public PageResponse<List<QuestionResponse>> getAllQuestions(int pageNo, int pageSize, String... sorts) {
        log.info("Getting all questions with pagination - page: {}, size: {}", pageNo, pageSize);
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<Question> page = questionRepository.findAllNotDeleted(pageable);
        
        List<QuestionResponse> responses = page.getContent()
                .stream()
                .map(questionMapper::toResponse)
                .toList();
        
        return PageResponse.<List<QuestionResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    public PageResponse<List<QuestionResponse>> getQuestionsByTeacher(String teacherId, int pageNo, int pageSize, String... sorts) {
        log.info("Getting questions by teacher id: {} with pagination - page: {}, size: {}", teacherId, pageNo, pageSize);
        
        userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<Question> page = questionRepository.findByCreatedByIdNotDeleted(teacherId, pageable);
        
        List<QuestionResponse> responses = page.getContent()
                .stream()
                .map(questionMapper::toResponse)
                .toList();
        
        return PageResponse.<List<QuestionResponse>>builder()
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
    public QuestionResponse updateQuestion(String id, QuestionUpdateRequest request) {
        log.info("Updating question with id: {}", id);
        
        Question question = questionRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        
        log.debug("Updating difficulty if provided: {}", request.difficultyId());
        if (request.difficultyId() != null && !request.difficultyId().trim().isEmpty()) {
            QuestionDifficulty difficulty = questionDifficultyRepository.findByIdNotDeleted(request.difficultyId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_DIFFICULTY_NOT_FOUND));
            question.setDifficulty(difficulty);
        }
        
        log.debug("Updating other fields of question");
        questionMapper.updateEntity(question, request);
        
        Question updatedQuestion = questionRepository.save(question);
        log.info("Successfully updated question with id: {}", updatedQuestion.getId());
        
        return questionMapper.toResponse(updatedQuestion);
    }
    
    @Override
    @Transactional
    public void deleteQuestion(String id) {
        log.info("Deleting question with id: {}", id);
        
        Question question = questionRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        
        question.setDeleted(true);
        questionRepository.save(question);
        
        log.info("Successfully deleted question with id: {}", id);
    }
    
    private String getCurrentUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
