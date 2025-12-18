package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.entity.TemporaryFile;
import com.fa25se225.capstone.repository.TemporaryFileRepository;
import com.fa25se225.capstone.service.implementation.CloudinaryService;
import com.fa25se225.capstone.service.implementation.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileUploadService.uploadQuestionFile(file));
    }
}