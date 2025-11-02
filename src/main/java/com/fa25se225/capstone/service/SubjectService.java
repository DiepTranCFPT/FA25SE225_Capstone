package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.SubjectCreationRequest;
import com.fa25se225.capstone.dto.request.SubjectUpdateRequest;
import com.fa25se225.capstone.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {
    
    SubjectResponse createSubject(SubjectCreationRequest request);
    
    SubjectResponse getSubjectById(String id);
    
    PageResponse<List<SubjectResponse>> getAllSubjects(int pageNo, int pageSize, String... sorts);
    
    SubjectResponse updateSubject(String id, SubjectUpdateRequest request);
    
    void deleteSubject(String id);
}
