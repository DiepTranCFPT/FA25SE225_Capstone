package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PermissionRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permission Management", description = "APIs for creating, viewing, and deleting permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Create a new permission",
            description = "Creates a new permission in the system. The permission name must be unique.")
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return ApiResponse.success(permissionService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all permissions",
            description = "Retrieves a list of all available permissions in the system.")
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.success(permissionService.getAll());
    }

    @DeleteMapping("/{permission}")
    @Operation(summary = "Delete a permission",
            description = "Deletes a permission by its name.")
    ApiResponse<String> delete(@PathVariable String permission) {
        permissionService.delete(permission);
        return ApiResponse.success("Delete permission successfully");
    }
}