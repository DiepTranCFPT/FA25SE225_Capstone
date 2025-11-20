package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.FileResponse;
import com.fa25se225.capstone.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MinioController {
    private final MinioService minioService;

    @Autowired
    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/minio/file/lesson")
    public ResponseEntity<ApiResponse<FileResponse>> getFileByNameLesson(@RequestParam String filename) {
        try {
            FileResponse file = minioService.getFileByName("lesson", filename);
            ApiResponse<FileResponse> response = ApiResponse.success(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.successWithMessage("Error retrieving file: " + e.getMessage()));
        }
    }

    @GetMapping("/minio/file/material")
    public ResponseEntity<ApiResponse<FileResponse>> getFileByNameMaterial(@RequestParam String filename) {
        try {
            FileResponse file = minioService.getFileByName("materials", filename);
            ApiResponse<FileResponse> response = ApiResponse.success(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.successWithMessage("Error retrieving file: " + e.getMessage()));
        }
    }

    @GetMapping("/minio/file")
    public ResponseEntity<ApiResponse<FileResponse>> getFileByName(@RequestParam String bucket, @RequestParam String filename) {
        try {
            FileResponse file = minioService.getFileByName(bucket, filename);
            ApiResponse<FileResponse> response = ApiResponse.success(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.successWithMessage("Error retrieving file: " + e.getMessage()));
        }
    }

    @GetMapping("/minio/file/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String bucket, @RequestParam String filename) {
        try {
            FileResponse file = minioService.getFileByName(bucket, filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.fileName())
                    .contentType(MediaType.parseMediaType(file.contentType()))
                    .body(file.bytes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
