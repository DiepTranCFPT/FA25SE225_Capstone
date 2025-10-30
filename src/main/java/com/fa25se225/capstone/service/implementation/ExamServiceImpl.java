package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.ExamCreationRequest;
import com.fa25se225.capstone.dto.response.ExamResponse;
import com.fa25se225.capstone.entity.Exam;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.ExamMapper;
import com.fa25se225.capstone.repository.ExamQuestionRepository;
import com.fa25se225.capstone.repository.ExamRepository;
import com.fa25se225.capstone.repository.SubjectRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.ExamService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamServiceImpl implements ExamService {

    ExamRepository examRepository;
    SubjectRepository subjectRepository;
    ExamQuestionRepository examQuestionRepository;
    ExamMapper examMapper;
    AccountUtil accountUtil;

    @Override
    @Transactional
    public ExamResponse createExam(ExamCreationRequest request) {
        log.info("Creating exam with title={}", request.getTitle());

        Exam exam = examMapper.toEntity(request, subjectRepository, examQuestionRepository);
        User currentUser = accountUtil.getCurrentUser();
        exam.setCreatedBy(currentUser);

        Exam saved = examRepository.saveAndFlush(exam);
        log.info("Exam created with id={}", saved.getId());

        return examMapper.toResponse(saved);
    }

    @Override
    public ExamResponse getExamById(String id) {
        Exam exam = examRepository.findByIdNotDeleted(id);
        if(Objects.isNull(exam)){
            throw new AppException(ErrorCode.EXAM_NOT_FOUND);
        }
        return examMapper.toResponse(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getAllExams() {
        List<Exam> exams = examRepository.findAllNotDeleted();
        return exams.stream()
                .map(examMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getAllExamsByUser() {
        List<Exam> exams = examRepository.findAllByUserIdNotDeleted(accountUtil.getCurrentUser().getId());
        return exams.stream()
                .map(examMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteExam(String id) {
        Exam exam = examRepository.findByIdNotDeleted(id);
        if (Objects.isNull(exam)) {
            throw new AppException(ErrorCode.EXAM_NOT_FOUND);
        }
        exam.setDeleted(true);
        examRepository.save(exam);
        log.info("Exam with id={} marked as deleted.", id);
    }
}
