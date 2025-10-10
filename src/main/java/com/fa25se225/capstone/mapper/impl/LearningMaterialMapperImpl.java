package com.fa25se225.capstone.mapper.impl;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.mapper.LearningMaterialMapper;
import org.springframework.stereotype.Component;

@Component
public class LearningMaterialMapperImpl implements LearningMaterialMapper {
    
    @Override
    public LearningMaterial toEntity(LearningMaterialCreationRequest request) {
        if (request == null) {
            return null;
        }
        
        return LearningMaterial.builder()
                .title(request.title())
                .description(request.description())
                .contentUrl(request.contentUrl())
                .isPublic(request.isPublic())
                .build();
    }
    
    @Override
    public LearningMaterialResponse toResponse(LearningMaterial entity) {
        if (entity == null) {
            return null;
        }
        
        return LearningMaterialResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .contentUrl(entity.getContentUrl())
                .typeId(entity.getType() != null ? entity.getType().getId() : null)
                .typeName(entity.getType() != null ? entity.getType().getName() : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .subjectName(entity.getSubject() != null ? entity.getSubject().getName() : null)
                .authorId(entity.getAuthor() != null ? entity.getAuthor().getId() : null)
                .authorName(entity.getAuthor() != null ? 
                    (entity.getAuthor().getFirstName() + " " + entity.getAuthor().getLastName()) : null)
                .isPublic(entity.getIsPublic())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    @Override
    public void updateEntity(LearningMaterial entity, LearningMaterialUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.contentUrl() != null) {
            entity.setContentUrl(request.contentUrl());
        }
        if (request.isPublic() != null) {
            entity.setIsPublic(request.isPublic());
        }
    }
}