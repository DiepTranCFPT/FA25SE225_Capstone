package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.Lesson;
import com.fa25se225.capstone.entity.MaterialType;
import com.fa25se225.capstone.entity.Permission;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.LearningMaterialMapper;
import com.fa25se225.capstone.repository.LearningMaterialRepository;
import com.fa25se225.capstone.repository.LessonRepository;
import com.fa25se225.capstone.repository.MaterialTypeRepository;
import com.fa25se225.capstone.repository.PermissionRepository;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.LearningMaterialService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningMaterialServiceImpl implements LearningMaterialService {
    
    private final LearningMaterialRepository learningMaterialRepository;
    private final MaterialTypeRepository materialTypeRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final LearningMaterialMapper learningMaterialMapper;
    private final PageHelper pageHelper;
    private final AccountUtil accountUtil;
    
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

        LearningMaterial savedMaterial = learningMaterialRepository.saveAndFlush(learningMaterial);
        log.info("Successfully created learning material with id: {}", savedMaterial.getId());

        String permissionName = "LEARNING_"+ savedMaterial.getId().trim();
        String permissionTitle = "LEARNING_"+savedMaterial.getTitle();

        Permission permission = new Permission(permissionName,permissionTitle,false);

        User account = accountUtil.getCurrentUser();
        account.setGrantedPermissions(Set.of(permission));


        userRepository.saveAndFlush(account);
        permissionRepository.saveAndFlush(permission);
        
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

    @Override
    @Transactional
    public LearningMaterialResponse registerLearningMaterial(String learningMaterialId) {
        log.info("Registering learning material with id: {}", learningMaterialId);

        // Get current user (student)
        String currentUserEmail = getCurrentUserEmail();
        User student = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Get learning material
        LearningMaterial learningMaterial = learningMaterialRepository.findByIdNotDeleted(learningMaterialId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_MATERIAL_NOT_FOUND));

        // Create permission name from learning material title
        String permissionName = "LEARNING_"+learningMaterialId;
        log.debug("Creating/Getting permission with name: {}", permissionName);

        // Check if user already has this permission
        boolean alreadyRegistered = student.getGrantedPermissions().stream()
                .anyMatch(p -> p.getName().equals(permissionName));

        if (alreadyRegistered) {
            log.warn("Student with id: {} already registered for learning material: {}", student.getId(), learningMaterialId);
            throw new AppException(ErrorCode.ALREADY_REGISTERED);
        }

        Permission permission = permissionRepository.findById(permissionName).orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        // Add permission to user's granted permissions
        student.getGrantedPermissions().add(permission);
        userRepository.save(student);

        log.info("Successfully registered student {} for learning material: {}", student.getId(), learningMaterialId);

        return learningMaterialMapper.toResponse(learningMaterial);
    }

    @Override
    public PageResponse<List<LearningMaterialResponse>> getRegisteredMaterials(int pageNo, int pageSize, String... sorts) {
        log.info("Getting registered learning materials for current user with pagination - page: {}, size: {}", pageNo, pageSize);

        // Get current user
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Get all granted permissions that start with "ACCESS_"
        Set<String> permissionNames = currentUser.getGrantedPermissions().stream()
                .map(Permission::getName)
                .filter(name -> name.startsWith("ACCESS_"))
                .collect(java.util.stream.Collectors.toSet());

        log.debug("Found {} permissions with ACCESS_ prefix", permissionNames.size());

        if (permissionNames.isEmpty()) {
            // Return empty page if user has no registered materials
            return PageResponse.<List<LearningMaterialResponse>>builder()
                    .pageNo(pageNo)
                    .pageSize(pageSize)
                    .totalPage(0)
                    .totalElement(0L)
                    .sortBy(sorts)
                    .items(List.of())
                    .build();
        }

        // Extract titles from permission names
        // Permission format: "ACCESS_" + title.toUpperCase().replaceAll("\\s+", "_")
        // We need to get all materials and filter by matching permissions
        List<LearningMaterial> allMaterials = learningMaterialRepository.findAllNotDeleted();

        List<LearningMaterial> registeredMaterials = allMaterials.stream()
                .filter(material -> {
                    String expectedPermissionName = "ACCESS_" + material.getTitle().toUpperCase().replaceAll("\\s+", "_");
                    return permissionNames.contains(expectedPermissionName);
                })
                .toList();

        log.debug("Found {} registered materials", registeredMaterials.size());

        // Apply pagination manually
        int start = pageNo * pageSize;
        int end = Math.min(start + pageSize, registeredMaterials.size());

        List<LearningMaterial> paginatedMaterials;
        if (start >= registeredMaterials.size()) {
            paginatedMaterials = List.of();
        } else {
            paginatedMaterials = registeredMaterials.subList(start, end);
        }

        List<LearningMaterialResponse> responses = paginatedMaterials.stream()
                .map(learningMaterialMapper::toResponse)
                .toList();

        int totalPages = (int) Math.ceil((double) registeredMaterials.size() / pageSize);

        return PageResponse.<List<LearningMaterialResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(totalPages)
                .totalElement((long) registeredMaterials.size())
                .sortBy(sorts)
                .items(responses)
                .build();
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}