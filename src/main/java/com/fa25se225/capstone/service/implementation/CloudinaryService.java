package com.fa25se225.capstone.service.implementation;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName){
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "auto"
                    ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary. Folder: {}", folderName, e);
            throw new AppException(ErrorCode.INVALID_IO);
        }
    }

    public void deleteFile(String publicId){
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary. PublicId: {}", publicId, e);
            throw new RuntimeException(e);
        }
    }

    public String uploadFileFromUrl(String imageUrl, String folderName) {
        try {
            Map uploadResult = cloudinary.uploader().upload(imageUrl, ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "auto"
            ));

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload image from URL: {} to folder: {}", imageUrl, folderName, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public String getPublicIdFromUrl(String url){
        // https://res.cloudinary.com/demo/image/upload/v1583225224/user_avatars/my_avatar.jpg

        // /upload/v1583225224/user_avatars/my_avatar.jpg
        final String uploadMarker = "/upload/";
        int pathStartIndex = url.indexOf(uploadMarker);
        if (pathStartIndex == -1) {
            return null;
        }
        // v1583225224/user_avatars/my_avatar.jpg
        pathStartIndex += uploadMarker.length();

        // v1583225224/user_avatars/my_avatar
        int pathEndIndex = url.lastIndexOf('.');
        if (pathEndIndex == -1 || pathEndIndex < pathStartIndex) {
            return null;
        }

        String versionAndPublicId = url.substring(pathStartIndex, pathEndIndex);

        // user_avatars/my_avatar
        if (versionAndPublicId.matches("^v\\d+/.+")) {
            return versionAndPublicId.substring(versionAndPublicId.indexOf('/') + 1);
        }

        return versionAndPublicId;
    }


}
