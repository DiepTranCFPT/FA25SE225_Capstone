package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.LinkStudentRequest;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.request.ParentProfileUpdateRequest;
import com.fa25se225.capstone.dto.request.UnlinkStudentRequest;
import com.fa25se225.capstone.dto.response.ChildOverviewResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;
import com.fa25se225.capstone.entity.ParentProfile;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.entity.v2.AttemptStatusV2;
import com.fa25se225.capstone.entity.v2.ExamAttemptV2;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.ParentProfileMapper;
import com.fa25se225.capstone.mapper.v2.ExamAttemptV2Mapper;
import com.fa25se225.capstone.repository.ParentProfileRepository;
import com.fa25se225.capstone.repository.StudentProfileRepository;
import com.fa25se225.capstone.repository.v2.ExamAttemptV2Repository;
import com.fa25se225.capstone.service.ParentService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentServiceImpl implements ParentService {

    private final ParentProfileRepository parentRepository;
    private final StudentProfileRepository studentRepository;
    private final ExamAttemptV2Repository attemptRepository;
    private final AccountUtil accountUtil;
    private final ExamAttemptV2Mapper attemptMapper;
    private final PageHelper pageHelper;
    private final ParentProfileMapper parentProfileMapper;

    @Override
    @Transactional
    @CacheEvict(value = "children_overview", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void linkStudent(LinkStudentRequest request) {
        User parentUser = accountUtil.getCurrentUser();
        ParentProfile parentProfile = findParentProfileByUserIdOrElseThrowException(parentUser.getId());

        StudentProfile studentProfile = studentRepository.findByUserEmail(request.studentEmail())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_PROFILE_NOT_FOUND));

        if (Objects.isNull(studentProfile.getConnectionCode()) ||
                !studentProfile.getConnectionCode().equals(request.connectionCode())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (studentProfile.getConnectionCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRED_OTP);
        }

        if (parentProfile.getChildren().contains(studentProfile)) {
            throw new AppException(ErrorCode.ALREADY_LINKED_STUDENT);
        }

        parentProfile.getChildren().add(studentProfile);

        studentProfile.setConnectionCode(null);
        studentProfile.setConnectionCodeExpiry(null);

        parentRepository.save(parentProfile);
        studentRepository.save(studentProfile);

        log.info("Linked parent {} with student {}", parentUser.getEmail(), request.studentEmail());
    }

    @Override
    @Transactional
    @CacheEvict(value = "children_overview", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void unlinkStudent(UnlinkStudentRequest request) {
        User parentUser = accountUtil.getCurrentUser();

        ParentProfile parentProfile = findParentProfileByUserIdOrElseThrowException(parentUser.getId());

        boolean removed = parentProfile.getChildren().removeIf(student ->
                student.getUser().getEmail().equals(request.studentEmail())
        );

        if (!removed) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        parentRepository.save(parentProfile);
        log.info("Unlinked student {} from parent {}", request.studentEmail(), parentUser.getEmail());
    }

    private ParentProfile findParentProfileByUserIdOrElseThrowException(String parentId){
        return parentRepository.findByUserId(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_PROFILE_NOT_FOUND));
    }

    @Override
    @Cacheable(value = "children_overview", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public List<ChildOverviewResponse> getChildrenOverview() {
        User parentUser = accountUtil.getCurrentUser();
        ParentProfile parentProfile = findParentProfileByUserIdOrElseThrowException(parentUser.getId());

        List<ChildOverviewResponse> response = new ArrayList<>();

        for (StudentProfile child : parentProfile.getChildren()) {
            String userId = child.getUser().getId();

            long totalExams = attemptRepository.countByUserIdAndStatus(userId, AttemptStatusV2.COMPLETED);
            Double avgScore = attemptRepository.getAverageScoreByUserId(userId);
            Optional<ExamAttemptV2> lastAttempt = attemptRepository.findFirstByUserIdAndStatusOrderByEndTimeDesc(userId, AttemptStatusV2.COMPLETED);

            response.add(ChildOverviewResponse.builder()
                    .studentId(userId)
                    .studentName(child.getUser().getFirstName() + " " + child.getUser().getLastName())
                    .email(child.getUser().getEmail())
                    .avatarUrl(child.getUser().getImgUrl())
                    .totalExamsTaken(totalExams)
                    .averageScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0)
                    .lastExamTitle(lastAttempt.map(a -> a.getExam().getTitle()).orElse("N/A"))
                    .lastExamScore(lastAttempt.map(ExamAttemptV2::getScore).orElse(0.0))
                    .lastActivity(lastAttempt.map(ExamAttemptV2::getEndTime).orElse(null))
                    .build());
        }
        return response;
    }

    @Override
    @Cacheable(value = "child_exam_history", key = "#studentId + '_' + #pageNo + '_' + #pageSize + '_' + T(java.util.Arrays).toString(#sorts)")
    public PageResponse<List<ExamAttemptV2Response>> getChildExamHistory(String studentId, int pageNo, int pageSize, String... sorts) {
        User parentUser = accountUtil.getCurrentUser();
        ParentProfile parentProfile = parentRepository.findByUserId(parentUser.getId())
                .orElseThrow(() -> new RuntimeException("Parent profile not found"));

        boolean isMyChild = parentProfile.getChildren().stream()
                .anyMatch(child -> child.getUser().getId().equals(studentId));

        if (!isMyChild) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<ExamAttemptV2> page = attemptRepository.findByUserId(studentId, pageable);

        List<ExamAttemptV2Response> items = page.getContent().stream()
                .map(attemptMapper::toResponse)
                .toList();

        return PageResponse.<List<ExamAttemptV2Response>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .items(items)
                .build();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "parent_profile", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"),
            @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    })
    public void updateProfile(ParentProfileUpdateRequest request) {
        User parentUser = accountUtil.getCurrentUser();
        ParentProfile profile = findParentProfileByUserIdOrElseThrowException(parentUser.getId());
        parentProfileMapper.updateProfile(profile, request);

        parentRepository.save(profile);

    }
}
