package com.fa25se225.capstone.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String url;
    private String accessKey;
    private String secretKey;
    private Bucket bucket;

    @Data
    public static class Bucket {
        private String avatars;
        private String materials;
        private String lesson;
    }
}
