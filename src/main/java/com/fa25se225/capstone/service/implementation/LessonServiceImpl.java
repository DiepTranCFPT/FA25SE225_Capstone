package com.fa25se225.capstone.service.implementation;

import com.cloudinary.provisioning.Account;
import com.fa25se225.capstone.dto.request.LessonCreationRequest;
import com.fa25se225.capstone.dto.request.LessonDTO;
import com.fa25se225.capstone.dto.request.LessonUpdateRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.LessonResponse;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.Lesson;
import com.fa25se225.capstone.entity.Permission;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.QuestionV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.LessonMapper;
import com.fa25se225.capstone.repository.LearningMaterialRepository;
import com.fa25se225.capstone.repository.LessonRepository;
import com.fa25se225.capstone.repository.PermissionRepository;
import com.fa25se225.capstone.repository.v2.QuestionV2Repository;
import com.fa25se225.capstone.service.LessonService;
import com.fa25se225.capstone.service.helper.MinioFileService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final QuestionV2Repository questionRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final LessonMapper lessonMapper;
    private final PageHelper pageHelper;
    private final AccountUtil accountUtil;
    private final PermissionRepository permissionRepository;
    private final MinioFileService minioFileService;
    private static final String bucketName = "lesson";

    @Override
    @Transactional
    public LessonResponse create(LessonCreationRequest request,MultipartFile file) {
        log.info("Creating lesson with name: {}", request.name());
        User user = accountUtil.getCurrentUser();

        QuestionV2 question = null;
        if (request.questionId() != null && !request.questionId().trim().isEmpty()) {
            log.debug("Validating and getting question with id: {}", request.questionId());
            question = questionRepository.findById(request.questionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
            if(question.getDeleted() == true){
                throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
            }
        }

        LearningMaterial learningMaterial = null;
        if (request.learningMaterialId() != null && !request.learningMaterialId().trim().isEmpty()) {
            log.debug("Validating and getting learning material with id: {}", request.learningMaterialId());
            learningMaterial = learningMaterialRepository.findByIdNotDeleted(request.learningMaterialId())
                    .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
        }


        log.debug("Creating lesson entity from request");
        Lesson lesson = lessonMapper.toEntity(request);
        lesson.setQuestion(question);
        lesson.setLearningMaterial(learningMaterial);

        Lesson savedLesson = lessonRepository.saveAndFlush(lesson);
        log.info("Successfully created lesson with id: {}", savedLesson.getId());
        String nameFile = "LESSON_"+ "_" + savedLesson.getId();

        try {
            minioFileService.uploadFile(bucketName, nameFile, file);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return lessonMapper.toResponse(savedLesson);
    }

    @Override
    @Transactional
    public LessonResponse getById(String id) {
        log.info("Getting lesson by id: {}", id);

        Lesson lesson = lessonRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        String permissions = "LEARNING_" + lesson.getLearningMaterial().getId().trim();
        User account = accountUtil.getCurrentUser();
        boolean hasPermission = account.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permissions));

        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission: " + permissions);
        }
        return lessonMapper.toResponse(lesson);
    }

    @Override
    public PageResponse<List<LessonResponse>> getAll(int pageNo, int pageSize, String... sorts) {
        log.info("Getting all lessons with pagination - page: {}, size: {}", pageNo, pageSize);

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<Lesson> page = lessonRepository.findAllNotDeleted(pageable);

        List<LessonResponse> responses = page.getContent()
                .stream()
                .map(lessonMapper::toResponse)
                .toList();

        return PageResponse.<List<LessonResponse>>builder()
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
    public LessonResponse update(String id, LessonUpdateRequest request) {
        log.info("Updating lesson with id: {}", id);

        Lesson lesson = lessonRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        String permissions = "LEARNING_" + lesson.getLearningMaterial().getId().trim();
        User account = accountUtil.getCurrentUser();
        boolean hasPermission = account.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permissions));

        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission: " + permissions);
        }

        log.debug("Updating question if provided: {}", request.questionId());
        if (request.questionId() != null) {
            if (request.questionId().trim().isEmpty()) {
                lesson.setQuestion(null);
            } else {
                QuestionV2 question = questionRepository.findById(request.questionId())
                        .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
                if (question.getDeleted() == true) {
                    throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
                }
                lesson.setQuestion(question);
            }
        }

        log.debug("Updating learning material if provided: {}", request.learningMaterialId());
        if (request.learningMaterialId() != null) {
            if (request.learningMaterialId().trim().isEmpty()) {
                lesson.setLearningMaterial(null);
            } else {
                LearningMaterial learningMaterial = learningMaterialRepository.findByIdNotDeleted(request.learningMaterialId())
                        .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
                lesson.setLearningMaterial(learningMaterial);
            }
        }

        log.debug("Updating other fields of lesson");
        lessonMapper.updateEntity(lesson, request);

        Lesson updatedLesson = lessonRepository.save(lesson);
        log.info("Successfully updated lesson with id: {}", updatedLesson.getId());

        return lessonMapper.toResponse(updatedLesson);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Deleting lesson with id: {}", id);

        Lesson lesson = lessonRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        String permissions = "LEARNING_" + lesson.getLearningMaterial().getId().trim();
        User account = accountUtil.getCurrentUser();
        boolean hasPermission = account.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permissions));

        if (!hasPermission) {
            throw new AccessDeniedException("You do not have permission: " + permissions);
        }

        log.debug("Performing soft delete for lesson with id: {}", id);
        lesson.setDeleted(true);
        lessonRepository.save(lesson);

        log.info("Successfully deleted lesson with id: {}", id);
    }

    @Override
    public PageResponse<List<LessonResponse>> getLessonsByLearningMaterial(String learningMaterialId, int pageNo, int pageSize, String... sorts) {
        log.info("Getting lessons by learning material: {} with pagination - page: {}, size: {}", learningMaterialId, pageNo, pageSize);

        // Validate learning material exists
        learningMaterialRepository.findByIdNotDeleted(learningMaterialId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<Lesson> page = lessonRepository.findByLearningMaterialIdNotDeleted(learningMaterialId, pageable);

        List<LessonResponse> responses = page.getContent()
                .stream()
                .map(lessonMapper::toResponse)
                .toList();

        return PageResponse.<List<LessonResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
}
