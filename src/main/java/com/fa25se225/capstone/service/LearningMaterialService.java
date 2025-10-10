package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.LearningMaterialCreationRequest;
import com.fa25se225.capstone.dto.request.LearningMaterialUpdateRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.LearningMaterialResponse;

import java.util.List;

public interface LearningMaterialService {
    
    LearningMaterialResponse create(LearningMaterialCreationRequest request);
    
    LearningMaterialResponse getById(String id);
    
    PageResponse<List<LearningMaterialResponse>> getAll(int pageNo, int pageSize, String... sorts);
    
    PageResponse<List<LearningMaterialResponse>> getMyMaterials(int pageNo, int pageSize, String... sorts);
    
    PageResponse<List<LearningMaterialResponse>> getPublicMaterials(int pageNo, int pageSize, String... sorts);
    
    PageResponse<List<LearningMaterialResponse>> getBySubject(String subjectId, int pageNo, int pageSize, String... sorts);
    
    PageResponse<List<LearningMaterialResponse>> getByType(String typeId, int pageNo, int pageSize, String... sorts);
    
    PageResponse<List<LearningMaterialResponse>> searchByKeyword(String keyword, int pageNo, int pageSize, String... sorts);
    
    LearningMaterialResponse update(String id, LearningMaterialUpdateRequest request);
    
    void delete(String id);
}