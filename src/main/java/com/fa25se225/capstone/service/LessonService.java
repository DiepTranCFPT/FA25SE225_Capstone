package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.LessonCreationRequest;
import com.fa25se225.capstone.dto.request.LessonUpdateRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.response.LessonResponse;

import java.util.List;

public interface LessonService {
    LessonResponse create(LessonCreationRequest request);

    LessonResponse getById(String id);

    PageResponse<List<LessonResponse>> getAll(int pageNo, int pageSize, String... sorts);

    LessonResponse update(String id, LessonUpdateRequest request);

    void delete(String id);

    PageResponse<List<LessonResponse>> getLessonsByLearningMaterial(String learningMaterialId, int pageNo, int pageSize, String... sorts);
}
