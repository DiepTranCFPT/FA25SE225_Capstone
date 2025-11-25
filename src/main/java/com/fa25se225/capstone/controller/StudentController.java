package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.StudentProfileUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/connection-code")
    public ApiResponse<String> generateCode() {
        return ApiResponse.success(studentService.generateConnectionCode());
    }

    @PutMapping("/me")
    public ApiResponse<String> updateProfile(@RequestBody StudentProfileUpdateRequest request) {
        studentService.updateProfile(request);
        return ApiResponse.success("Update successfully");
    }
}