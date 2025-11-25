package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.StudentProfileRepository;
import com.fa25se225.capstone.service.StudentService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentRepository;
    private final AccountUtil accountUtil;

    @Override
    @Transactional
    public String generateConnectionCode() {
        User studentUser = accountUtil.getCurrentUser();
        StudentProfile profile = studentRepository.findByUserId(studentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_PROFILE_NOT_FOUND));

        String code = String.format("%06d", new Random().nextInt(999999));

        profile.setConnectionCode(code);
        profile.setConnectionCodeExpiry(LocalDateTime.now().plusMinutes(15));

        studentRepository.save(profile);
        return code;
    }
}