package com.fa25se225.capstone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
public class CommunityUpdateRequest {
    private String name;
    private String description;
    private MultipartFile image;
}
