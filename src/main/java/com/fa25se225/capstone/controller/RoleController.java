package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.request.PermissionAssignmentRequest;
import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.dto.response.RoleResponse;
import com.fa25se225.capstone.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.success(roleService.getAll());
    }

    @DeleteMapping("/{roleName}")
    ApiResponse<String> delete(@PathVariable String roleName) {
        roleService.delete(roleName);
        return ApiResponse.success("Delete role successfully");
    }

    @GetMapping("/{roleName}/permissions")
    ApiResponse<List<PermissionResponse>> getPermissionsOfRole(@PathVariable String roleName) {
        return ApiResponse.success(roleService.getPermissions(roleName));
    }

    @PostMapping("/{roleName}/permissions")
    ApiResponse<RoleResponse> assignPermissionsToRole(@PathVariable String roleName, @RequestBody PermissionAssignmentRequest request) {
        return ApiResponse.success(roleService.assignPermissions(roleName, request));
    }

    @DeleteMapping("/{roleName}/permissions")
    ApiResponse<RoleResponse> revokePermissionsFromRole(@PathVariable String roleName, @RequestBody PermissionAssignmentRequest request) {
        return ApiResponse.success(roleService.revokePermissions(roleName, request));
    }
}
