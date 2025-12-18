package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.entity.TemporaryFile;
import com.fa25se225.capstone.repository.TemporaryFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
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


    public void confirmFileUsage(String imageUrl) {
        if (Strings.isNotBlank(imageUrl)) {
            temporaryFileRepository.findByUrl(imageUrl)
                    .ifPresent(temporaryFileRepository::delete);
        }
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupOrphanedImages() {
        log.info("Running orphaned file cleanup job...");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<TemporaryFile> orphans = temporaryFileRepository.findByCreatedAtBefore(cutoff);

        if (orphans.isEmpty()) {
            return;
        }

        log.info("Found {} orphaned files. Deleting...", orphans.size());

        for (TemporaryFile file : orphans) {
            try {
                if (file.getPublicId() != null) {
                    cloudinaryService.deleteFile(file.getPublicId());
                }
            } catch (Exception e) {
                log.error("Failed to delete file from Cloudinary: {}", file.getPublicId(), e);
            }
        }
        temporaryFileRepository.deleteAll(orphans);
        log.info("Cleanup completed.");
    }


}