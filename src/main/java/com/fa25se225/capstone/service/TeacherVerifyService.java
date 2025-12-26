package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.TeacherVerificationRequestDto;

import java.util.List;

public interface TeacherVerifyService {
    TeacherVerificationRequestDto createRequestTeacherVerify();
    List<TeacherVerificationRequestDto> getAllRequestTeacherVerification();
}
