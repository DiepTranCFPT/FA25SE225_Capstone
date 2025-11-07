package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.MaterialType;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.LearningMaterialMapper;
import com.fa25se225.capstone.repository.LearningMaterialRepository;
import com.fa25se225.capstone.repository.MaterialTypeRepository;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.LearningMaterialService;
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
public class LearningMaterialServiceImpl implements LearningMaterialService {
    
    private final LearningMaterialRepository learningMaterialRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final LearningMaterialMapper learningMaterialMapper;
    private final PageHelper pageHelper;
    
    @Override
    @Transactional
    public LearningMaterialResponse create(LearningMaterialCreationRequest request) {
        log.info("Creating learning material with title: {}", request.title());
        
        log.debug("Getting current user for learning material creation");
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        log.debug("Validating and getting material type with id: {}", request.typeId());
        MaterialType materialType = materialTypeRepository.findByIdNotDeleted(request.typeId())
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_TYPE_NOT_FOUND));
        
        log.debug("Validating and getting subject if provided: {}", request.subjectId());
        Subject subject = null;
        if (request.subjectId() != null && !request.subjectId().trim().isEmpty()) {
            subject = subjectRepository.findByIdNotDeleted(request.subjectId())
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        }
        
        log.debug("Creating learning material entity from request");
        LearningMaterial learningMaterial = learningMaterialMapper.toEntity(request);
        learningMaterial.setAuthor(currentUser);
        learningMaterial.setType(materialType);
        learningMaterial.setSubject(subject);
        
        if (learningMaterial.getIsPublic() == null) {
            learningMaterial.setIsPublic(false);
        }
        
        LearningMaterial savedMaterial = learningMaterialRepository.save(learningMaterial);
        log.info("Successfully created learning material with id: {}", savedMaterial.getId());
        
        return learningMaterialMapper.toResponse(savedMaterial);
    }
    
    @Override
    public LearningMaterialResponse getById(String id) {
        log.info("Getting learning material by id: {}", id);
        
        LearningMaterial learningMaterial = learningMaterialRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
        
        log.debug("Checking if user has permission to view material with id: {}", id);
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        if (!learningMaterial.getIsPublic() && !learningMaterial.getAuthor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return learningMaterialMapper.toResponse(learningMaterial);
    }
    
    @Override
    public PageResponse<List<LearningMaterialResponse>> getAll(int pageNo, int pageSize, String... sorts) {
        log.info("Getting all learning materials with pagination - page: {}, size: {}", pageNo, pageSize);
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<LearningMaterial> page = learningMaterialRepository.findAllNotDeleted(pageable);
        
        List<LearningMaterialResponse> responses = page.getContent()
                .stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
        
        return PageResponse.<List<LearningMaterialResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    public PageResponse<List<LearningMaterialResponse>> getMyMaterials(int pageNo, int pageSize, String... sorts) {
        log.info("Getting my learning materials with pagination - page: {}, size: {}", pageNo, pageSize);
        
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<LearningMaterial> page = learningMaterialRepository.findByAuthorIdNotDeleted(currentUser.getId(), pageable);
        
        List<LearningMaterialResponse> responses = page.getContent()
                .stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
        
        return PageResponse.<List<LearningMaterialResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    public PageResponse<List<LearningMaterialResponse>> getPublicMaterials(int pageNo, int pageSize, String... sorts) {
        log.info("Getting public learning materials with pagination - page: {}, size: {}", pageNo, pageSize);
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<LearningMaterial> page = learningMaterialRepository.findAllPublicNotDeleted(pageable);
        
        List<LearningMaterialResponse> responses = page.getContent()
                .stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
        
        return PageResponse.<List<LearningMaterialResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    public PageResponse<List<LearningMaterialResponse>> getBySubject(String subjectId, int pageNo, int pageSize, String... sorts) {
        log.info("Getting learning materials by subject: {} with pagination - page: {}, size: {}", subjectId, pageNo, pageSize);
        
        log.debug("Validating subject exists with id: {}", subjectId);
        subjectRepository.findByIdNotDeleted(subjectId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<LearningMaterial> page = learningMaterialRepository.findBySubjectIdNotDeleted(subjectId, pageable);
        
        List<LearningMaterialResponse> responses = page.getContent()
                .stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
        
        return PageResponse.<List<LearningMaterialResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    public PageResponse<List<LearningMaterialResponse>> getByType(String typeId, int pageNo, int pageSize, String... sorts) {
        log.info("Getting learning materials by type: {} with pagination - page: {}, size: {}", typeId, pageNo, pageSize);
        
        log.debug("Validating material type exists with id: {}", typeId);
        materialTypeRepository.findByIdNotDeleted(typeId)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_TYPE_NOT_FOUND));
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<LearningMaterial> page = learningMaterialRepository.findByTypeIdNotDeleted(typeId, pageable);
        
        List<LearningMaterialResponse> responses = page.getContent()
                .stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
        
        return PageResponse.<List<LearningMaterialResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(responses)
                .build();
    }
    
    @Override
    public PageResponse<List<LearningMaterialResponse>> searchByKeyword(String keyword, int pageNo, int pageSize, String... sorts) {
        log.info("Searching learning materials by keyword: {} with pagination - page: {}, size: {}", keyword, pageNo, pageSize);
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<LearningMaterial> page = learningMaterialRepository.findByKeywordNotDeleted(keyword, pageable);
        
        List<LearningMaterialResponse> responses = page.getContent()
                .stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
        
        return PageResponse.<List<LearningMaterialResponse>>builder()
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
    public LearningMaterialResponse update(String id, LearningMaterialUpdateRequest request) {
        log.info("Updating learning material with id: {}", id);
        
        LearningMaterial learningMaterial = learningMaterialRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
        
        log.debug("Checking if current user is the author of learning material with id: {}", id);
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        if (!learningMaterial.getAuthor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        log.debug("Updating material type if provided: {}", request.typeId());
        if (request.typeId() != null && !request.typeId().trim().isEmpty()) {
            MaterialType materialType = materialTypeRepository.findByIdNotDeleted(request.typeId())
                    .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_TYPE_NOT_FOUND));
            learningMaterial.setType(materialType);
        }
        
        log.debug("Updating subject if provided: {}", request.subjectId());
        if (request.subjectId() != null) {
            if (request.subjectId().trim().isEmpty()) {
                learningMaterial.setSubject(null);
            } else {
                Subject subject = subjectRepository.findByIdNotDeleted(request.subjectId())
                        .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
                learningMaterial.setSubject(subject);
            }
        }
        
        log.debug("Updating other fields of learning material");
        learningMaterialMapper.updateEntity(learningMaterial, request);
        
        LearningMaterial updatedMaterial = learningMaterialRepository.save(learningMaterial);
        log.info("Successfully updated learning material with id: {}", updatedMaterial.getId());
        
        return learningMaterialMapper.toResponse(updatedMaterial);
    }
    
    @Override
    @Transactional
    public void delete(String id) {
        log.info("Deleting learning material with id: {}", id);
        
        LearningMaterial learningMaterial = learningMaterialRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));
        
        log.debug("Checking if current user is the author of learning material to delete with id: {}", id);
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        if (!learningMaterial.getAuthor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        log.debug("Performing soft delete for learning material with id: {}", id);
        learningMaterial.setDeleted(true);
        learningMaterialRepository.save(learningMaterial);
        
        log.info("Successfully deleted learning material with id: {}", id);
    }
    
    @Override
    public List<LearningMaterialResponse> getAllMaterials() {
        List<LearningMaterial> materials = learningMaterialRepository.findAllNotDeleted();
        return materials.stream()
                .map(learningMaterialMapper::toResponse)
                .toList();
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}