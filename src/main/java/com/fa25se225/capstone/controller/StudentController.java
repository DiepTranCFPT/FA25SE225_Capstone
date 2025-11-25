package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.service.StudentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/connection-code")
    public ApiResponse<String> generateCode() {
        return ApiResponse.success(studentService.generateConnectionCode());
    }
}