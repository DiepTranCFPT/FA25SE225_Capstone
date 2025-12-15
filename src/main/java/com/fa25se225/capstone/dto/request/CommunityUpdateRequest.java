package com.fa25se225.capstone.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CommunityUpdateRequest {
    private String name;
    private String description;
    private MultipartFile image;
}
