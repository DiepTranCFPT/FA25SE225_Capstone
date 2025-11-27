package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    FileResponse getFileByName(String bucket, String filename) throws Exception;
    String uploadFile(MultipartFile file,String fileName, String bucket) throws Exception;
    byte[] getFile(String fileName,String bucket) throws Exception;
    String uploadVideo(MultipartFile file,String objectName);
    String getVideoUrl(String objectName);
}
