package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.FileResponse;
import com.fa25se225.capstone.service.MinioService;
import com.fa25se225.capstone.utils.HelperTypeFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MinioController {

    @Value("${minio.bucket.lesson}")
    private String lessonBucket ;

    @Value("${minio.bucket.materials}")
    private String materialsBucket;

    private final MinioService minioService;
    private final HelperTypeFile helperTypeFile;

    @Autowired
    public MinioController(MinioService minioService, HelperTypeFile helperTypeFile) {
        this.minioService = minioService;
        this.helperTypeFile = helperTypeFile;
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

    @GetMapping("/{fileName}/lesson")
    public ResponseEntity<byte[]> getFileLesson(@PathVariable String fileName) throws Exception {
        byte[] data = minioService.getFile(fileName,lessonBucket);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/{fileName}/materials")
    public ResponseEntity<byte[]> getFileMaterial(@PathVariable String fileName) throws Exception {
        byte[] data = minioService.getFile(fileName,materialsBucket);
//        MediaType mediaType = helperTypeFile.getMediaType(fileName);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(data);
    }
    @GetMapping("/videos")
    public ResponseEntity<String> getVideos(@RequestParam String nameFile) {
        String videoUrl = minioService.getVideoUrl(nameFile);
        return ResponseEntity.ok(videoUrl);
    }
}
