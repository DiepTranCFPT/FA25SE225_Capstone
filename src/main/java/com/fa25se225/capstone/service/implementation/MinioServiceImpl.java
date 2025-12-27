package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.response.FileResponse;
import com.fa25se225.capstone.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-video}")
    private String bucketName;

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

    @Override
    public String uploadFile(MultipartFile file,String fileName, String bucket) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return fileName;
    }
    @Override
    public byte[] getFile(String fileName,String bucket) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .build()
        );
        return stream.readAllBytes();
    }
//    @Override
//    public String uploadVideo(MultipartFile file,String objectName) {
//        try {
//            boolean found = minioClient.bucketExists(
//                    BucketExistsArgs.builder().bucket(bucketName).build()
//            );
//            if (!found) {
//                minioClient.makeBucket(
//                        MakeBucketArgs.builder().bucket(bucketName).build()
//                );
//            }
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(objectName)
//                            .stream(file.getInputStream(), file.getSize(), -1)
//                            .contentType(file.getContentType())
//                            .build()
//            );
//            return objectName;
//        } catch (Exception e) {
//            throw new RuntimeException("Upload video failed", e);
//        }
//    }

    @Override
    public String getVideoUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(3, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Generate video URL failed", e);
        }
    }
    @Override
    public String uploadVideo(MultipartFile file, String objectName) {
        Path workDir = null;

        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            workDir = Files.createTempDirectory("upload-video-");

            Path input = workDir.resolve(file.getOriginalFilename());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, input, StandardCopyOption.REPLACE_EXISTING);
            }

            boolean isMp4 = isMp4(file, input);
            Path fileToUpload = input;

            if (isMp4) {
                Path fastMp4 = workDir.resolve("fast.mp4");
                runFfmpegFastStart(input, fastMp4);
                fileToUpload = fastMp4;
            }

            try (InputStream uploadStream = Files.newInputStream(fileToUpload)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(uploadStream, Files.size(fileToUpload), -1)
                                .contentType(isMp4 ? "video/mp4" : file.getContentType())
                                .build()
                );
            }

            return objectName;

        } catch (Exception e) {
            throw new RuntimeException("Upload video failed", e);
        } finally {
            // 5️⃣ cleanup file tạm
            if (workDir != null) {
                try (Stream<Path> walk = Files.walk(workDir)) {
                    walk.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                            });
                } catch (Exception ignored) {}
            }
        }
    }
    private boolean isMp4(MultipartFile file, Path input) {
        if ("video/mp4".equalsIgnoreCase(file.getContentType())) {
            return true;
        }
        return input.getFileName().toString().toLowerCase().endsWith(".mp4");
    }
    private void runFfmpegFastStart(Path input, Path output)
            throws IOException, InterruptedException {

        List<String> cmd = List.of(
                "ffmpeg",
                "-y",
                "-i", input.toString(),
                "-c", "copy",
                "-movflags", "+faststart",
                output.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            while (br.readLine() != null) {}
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("ffmpeg faststart failed, exit code=" + exit);
        }
    }


}
