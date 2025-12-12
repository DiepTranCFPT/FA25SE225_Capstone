package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.StudentProfileUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.StudentProfileMapper;
import com.fa25se225.capstone.mapper.UserMapper;
import com.fa25se225.capstone.repository.StudentProfileRepository;
import com.fa25se225.capstone.service.StudentService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentRepository;
    private final AccountUtil accountUtil;
    private final UserMapper userMapper;
    private final StudentProfileMapper studentProfileMapper;

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public String generateConnectionCode() {
        User studentUser = accountUtil.getCurrentUser();
        StudentProfile profile = getCurrentStudentProfileOrThrowException(studentUser);

        String code = String.format("%06d", new Random().nextInt(999999));

        profile.setConnectionCode(code);
        profile.setConnectionCodeExpiry(LocalDateTime.now().plusMinutes(15));

        studentRepository.save(profile);
        return code;
    }

    @Override
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void updateProfile(StudentProfileUpdateRequest request) {
        User studentUser = accountUtil.getCurrentUser();
        StudentProfile profile = getCurrentStudentProfileOrThrowException(studentUser);
        studentProfileMapper.updateProfile(profile, request);

        studentRepository.save(profile);
    }

    StudentProfile getCurrentStudentProfileOrThrowException(User studentUser){
        return studentRepository.findByUserId(studentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_PROFILE_NOT_FOUND));
    }
}
