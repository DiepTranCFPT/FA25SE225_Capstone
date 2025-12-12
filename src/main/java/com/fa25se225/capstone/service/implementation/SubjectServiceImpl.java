package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.request.SubjectCreationRequest;
import com.fa25se225.capstone.dto.request.SubjectUpdateRequest;
import com.fa25se225.capstone.dto.response.SubjectResponse;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.SubjectMapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.service.SubjectService;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final PageHelper pageHelper;
    
    @Override
    @Transactional
    @CacheEvict(value = "subjects_list", allEntries = true)
    public SubjectResponse createSubject(SubjectCreationRequest request) {
        log.info("Creating subject with name: {}", request.name());
        
        Subject subject = subjectMapper.toEntity(request);
        Subject savedSubject = subjectRepository.save(subject);
        
        log.info("Successfully created subject with id: {}", savedSubject.getId());
        return subjectMapper.toResponse(savedSubject);
    }
    
    @Override
    @Cacheable(value = "subjects", key = "#id")
    public SubjectResponse getSubjectById(String id) {
        log.info("Getting subject by id: {}", id);
        
        Subject subject = subjectRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        
        return subjectMapper.toResponse(subject);
    }
    
    @Override
    @Cacheable(value = "subjects_list", key = "#pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<SubjectResponse>> getAllSubjects(int pageNo, int pageSize, String... sorts) {
        log.info("Getting all subjects with pagination - page: {}, size: {}", pageNo, pageSize);
        
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<Subject> page = subjectRepository.findAllNotDeleted(pageable);
        
        List<SubjectResponse> responses = page.getContent()
                .stream()
                .map(subjectMapper::toResponse)
                .toList();
        
        return PageResponse.<List<SubjectResponse>>builder()
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
    @Caching(evict = {
        @CacheEvict(value = "subjects", key = "#id"),
        @CacheEvict(value = "subjects_list", allEntries = true)
    })
    public SubjectResponse updateSubject(String id, SubjectUpdateRequest request) {
        log.info("Updating subject with id: {}", id);
        
        Subject subject = subjectRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        
        subjectMapper.updateEntity(subject, request);
        Subject updatedSubject = subjectRepository.save(subject);
        
        log.info("Successfully updated subject with id: {}", updatedSubject.getId());
        return subjectMapper.toResponse(updatedSubject);
    }
    
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "subjects", key = "#id"),
        @CacheEvict(value = "subjects_list", allEntries = true)
    })
    public void deleteSubject(String id) {
        log.info("Deleting subject with id: {}", id);
        
        Subject subject = subjectRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        
        subject.setDeleted(true);
        subjectRepository.save(subject);
        
        log.info("Successfully deleted subject with id: {}", id);
    }
}
