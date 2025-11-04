package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.MaterialTypeRequest;
import com.fa25se225.capstone.dto.response.MaterialTypeResponse;
import com.fa25se225.capstone.service.MaterialTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material-types")
@RequiredArgsConstructor
public class MaterialTypeController {
    private final MaterialTypeService materialTypeService;

    @PostMapping
    public ResponseEntity<MaterialTypeResponse> create(@RequestBody MaterialTypeRequest request) {
        return ResponseEntity.ok(materialTypeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialTypeResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(materialTypeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MaterialTypeResponse>> getAll() {
        return ResponseEntity.ok(materialTypeService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialTypeResponse> update(@PathVariable String id, @RequestBody MaterialTypeRequest request) {
        return ResponseEntity.ok(materialTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        materialTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

