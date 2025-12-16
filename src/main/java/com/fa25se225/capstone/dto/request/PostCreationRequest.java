package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class PostCreationRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private MultipartFile image;
}
