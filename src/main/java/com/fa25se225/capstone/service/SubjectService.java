package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.SubjectCreationRequest;
import com.fa25se225.capstone.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {
    SubjectResponse create(SubjectCreationRequest request);
    SubjectResponse getById(String id);
    List<SubjectResponse> getAll();
    SubjectResponse update(String id, SubjectCreationRequest request);
    void delete(String id);
}

