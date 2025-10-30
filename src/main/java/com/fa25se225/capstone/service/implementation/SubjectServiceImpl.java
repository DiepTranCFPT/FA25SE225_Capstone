package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.SubjectCreationRequest;
import com.fa25se225.capstone.dto.response.SubjectResponse;
import com.fa25se225.capstone.entity.Subject;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.SubjectMapper;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.service.SubjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubjectServiceImpl implements SubjectService {

    SubjectRepository subjectRepository;
    SubjectMapper subjectMapper;

    @Override
    @Transactional
    public SubjectResponse create(SubjectCreationRequest request) {
        log.info("Creating subject: {}", request.getName());
        Subject subject = subjectMapper.toEntity(request);
        Subject saved = subjectRepository.save(subject);
        return subjectMapper.toResponse(saved);
    }

    @Override
    public SubjectResponse getById(String id) {
        Subject subject = subjectRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        return subjectMapper.toResponse(subject);
    }

    @Override
    public List<SubjectResponse> getAll() {
        List<Subject> subjects = subjectRepository.findAllNotDeleted();
        return subjects.stream().map(subjectMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public SubjectResponse update(String id, SubjectCreationRequest request) {
        log.info("Updating subject {}", id);
        Subject subject = subjectRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        if (request.getName() != null) subject.setName(request.getName());
        if (request.getDescription() != null) subject.setDescription(request.getDescription());

        Subject updated = subjectRepository.save(subject);
        return subjectMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Soft deleting subject {}", id);
        Subject subject = subjectRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
        subject.setDeleted(true);
        subjectRepository.save(subject);
    }
}
