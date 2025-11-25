package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.StudentProfileUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;

public interface StudentService {
    String generateConnectionCode();

    void updateProfile(StudentProfileUpdateRequest request);
}
