package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.MaterialTypeRequest;
import com.fa25se225.capstone.dto.response.MaterialTypeResponse;
import com.fa25se225.capstone.entity.MaterialType;
import com.fa25se225.capstone.repository.MaterialTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialTypeService implements IMaterialTypeService {
    private static final Logger log = LoggerFactory.getLogger(MaterialTypeService.class);
    private final MaterialTypeRepository materialTypeRepository;

    public MaterialTypeResponse create(MaterialTypeRequest request) {
        log.info("Creating MaterialType with name: {}", request.getName());
        MaterialType materialType = MaterialType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        materialType = materialTypeRepository.save(materialType);
        log.info("Created MaterialType with id: {}", materialType.getId());
        return toResponse(materialType);
    }

    public MaterialTypeResponse getById(String id) {
        log.info("Fetching MaterialType by id: {}", id);
        MaterialType materialType = materialTypeRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> {
                    log.warn("MaterialType not found for id: {}", id);
                    return new RuntimeException("MaterialType not found");
                });
        log.info("Found MaterialType: {}", materialType.getName());
        return toResponse(materialType);
    }

    public List<MaterialTypeResponse> getAll() {
        log.info("Fetching all non-deleted MaterialTypes");
        List<MaterialTypeResponse> result = materialTypeRepository.findAllNotDeleted().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.info("Found {} MaterialTypes", result.size());
        return result;
    }

    public MaterialTypeResponse update(String id, MaterialTypeRequest request) {
        log.info("Updating MaterialType id: {}", id);
        MaterialType materialType = materialTypeRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> {
                    log.warn("MaterialType not found for update, id: {}", id);
                    return new RuntimeException("MaterialType not found");
                });
        materialType.setName(request.getName());
        materialType.setDescription(request.getDescription());
        materialType = materialTypeRepository.save(materialType);
        log.info("Updated MaterialType id: {}", materialType.getId());
        return toResponse(materialType);
    }

    public void delete(String id) {
        log.info("Soft deleting MaterialType id: {}", id);
        MaterialType materialType = materialTypeRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> {
                    log.warn("MaterialType not found for delete, id: {}", id);
                    return new RuntimeException("MaterialType not found");
                });
        materialType.setDeleted(true);
        materialTypeRepository.save(materialType);
        log.info("MaterialType id: {} marked as deleted", id);
    }

    private MaterialTypeResponse toResponse(MaterialType materialType) {
        MaterialTypeResponse response = new MaterialTypeResponse();
        response.setId(materialType.getId());
        response.setCode(materialType.getCode());
        response.setName(materialType.getName());
        response.setDescription(materialType.getDescription());
        return response;
    }
}
