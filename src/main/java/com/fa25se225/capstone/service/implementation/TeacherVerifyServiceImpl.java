package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.VerificationStatus;
import com.fa25se225.capstone.dto.TeacherVerificationRequestDto;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherReviewResponse;
import com.fa25se225.capstone.entity.TeacherVerificationRequest;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.TeacherVerificationRequestRepository;

import com.fa25se225.capstone.service.TeacherProfileService;
import com.fa25se225.capstone.service.TeacherReviewService;
import com.fa25se225.capstone.service.TeacherVerifyService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherVerifyServiceImpl implements TeacherVerifyService {
    private final AccountUtil accountUtil;
    private final TeacherVerificationRequestRepository teacherVerifyRepository;
    private final TeacherProfileService  teacherProfileService;
    private final TeacherReviewService teacherReviewService;


    @Override
    public TeacherVerificationRequestDto createRequestTeacherVerify() {
        User user = accountUtil.getCurrentUser();
        boolean check = teacherVerifyRepository.existsByUserAndStatus(user, VerificationStatus.PENDING);
        if (check) {
            throw new AppException(ErrorCode.HAVING_REQUEST);
        }
        TeacherVerificationRequest request = new TeacherVerificationRequest();
        request.setUser(user);
        return convertToDTO(teacherVerifyRepository.save(request));
    }



    private TeacherVerificationRequestDto convertToDTO(TeacherVerificationRequest teacherVerificationRequest) {

        TeacherProfileResponse teacher = teacherProfileService.getProfileByUserId(teacherVerificationRequest.getUser().getId());

        TeacherVerificationRequestDto teacherVerificationRequestDto = new TeacherVerificationRequestDto();
        teacherVerificationRequestDto.setId(teacherVerificationRequest.getId());
        teacherVerificationRequestDto.setUserId(teacherVerificationRequest.getUser() != null ? teacherVerificationRequest.getUser().getId() : null);
        teacherVerificationRequestDto.setTeacherId(teacher.getId());
        teacherVerificationRequestDto.setStatus(teacherVerificationRequest.getStatus());
        teacherVerificationRequestDto.setNote(teacherVerificationRequest.getNote());
        teacherVerificationRequestDto.setReviewedById(teacherVerificationRequest.getReviewedBy() != null ? teacherVerificationRequest.getReviewedBy().getId() : null);
        teacherVerificationRequestDto.setReviewedAt(teacherVerificationRequest.getReviewedAt());
        teacherVerificationRequestDto.setCreatedAt(teacherVerificationRequest.getCreatedAt());
        teacherVerificationRequestDto.setUpdatedAt(teacherVerificationRequest.getUpdatedAt());
        return teacherVerificationRequestDto;
    }
}
