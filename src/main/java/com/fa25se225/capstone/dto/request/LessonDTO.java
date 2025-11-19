package com.fa25se225.capstone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonDTO {
    String name;

    MultipartFile file;

    String url;

    String questionId;

    String description;

    String learningMaterialId;
}
