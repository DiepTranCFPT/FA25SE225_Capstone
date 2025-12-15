package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostCreationRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String communityId;
    private MultipartFile image;
}
