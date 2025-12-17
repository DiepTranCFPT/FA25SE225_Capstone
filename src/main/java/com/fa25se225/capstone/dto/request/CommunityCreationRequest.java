package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.entity.Subject;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@Builder
public class CommunityCreationRequest {
    private String name;
    private String description;
    private MultipartFile image;
    private String subjectId;
}
