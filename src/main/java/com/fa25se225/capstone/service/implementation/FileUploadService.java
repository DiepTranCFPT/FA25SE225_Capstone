package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.entity.TemporaryFile;
import com.fa25se225.capstone.repository.TemporaryFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final CloudinaryService cloudinaryService;
    private final TemporaryFileRepository temporaryFileRepository;
    private final static String QUESTION_IMAGES_FOLDER = "question_images_posts";

    public String uploadQuestionFile(MultipartFile file){
        String url = cloudinaryService.uploadFile(file, QUESTION_IMAGES_FOLDER);
        String publicId = cloudinaryService.getPublicIdFromUrl(url);
        if (Objects.nonNull(publicId)) {
            TemporaryFile temp = TemporaryFile.builder()
                    .url(url)
                    .publicId(publicId)
                    .build();
            temporaryFileRepository.save(temp);
        }
        return url;
    }


}
