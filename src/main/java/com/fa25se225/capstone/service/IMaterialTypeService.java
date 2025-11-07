package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.MaterialTypeRequest;
import com.fa25se225.capstone.dto.response.MaterialTypeResponse;
import java.util.List;

public interface IMaterialTypeService {
    MaterialTypeResponse create(MaterialTypeRequest request);
    MaterialTypeResponse getById(String id);
    List<MaterialTypeResponse> getAll();
    MaterialTypeResponse update(String id, MaterialTypeRequest request);
    void delete(String id);
}

