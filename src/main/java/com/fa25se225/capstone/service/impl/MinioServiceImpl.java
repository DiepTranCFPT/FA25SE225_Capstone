package com.fa25se225.capstone.service.impl;

import com.fa25se225.capstone.dto.response.FileResponse;
import com.fa25se225.capstone.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Autowired
    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public FileResponse getFileByName(String bucket, String filename) throws Exception {
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(filename)
                        .build()
        );
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filename)
                        .build()
        )) {
            return new FileResponse(is.readAllBytes(), stat.contentType(), filename);
        }
    }
}
