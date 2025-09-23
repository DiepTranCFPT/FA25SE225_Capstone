package com.fa25se225.capstone.controller;


import com.fa25se225.capstone.dto.request.PermissionAssignmentRequest;
import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.dto.response.RoleResponse;
import com.fa25se225.capstone.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Role Management", description = "APIs for managing user roles and their permissions")
public class RoleController {
    RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role",
            description = "Creates a new role. The role name must be unique.")
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all roles",
            description = "Retrieves a list of all available roles in the system.")
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.success(roleService.getAll());
    }

    @DeleteMapping("/{roleName}")
    @Operation(summary = "Delete a role",
            description = "Deletes a role by its name. Note: This is a hard delete.")
    ApiResponse<String> delete(@PathVariable String roleName) {
        roleService.delete(roleName);
        return ApiResponse.success("Delete role successfully");
    }

    @GetMapping("/{roleName}/permissions")
    @Operation(summary = "Get permissions of a role",
            description = "Retrieves a list of all permissions associated with a specific role.")
    ApiResponse<List<PermissionResponse>> getPermissionsOfRole(@PathVariable String roleName) {
        return ApiResponse.success(roleService.getPermissions(roleName));
    }

    @PostMapping("/{roleName}/permissions")
    @Operation(summary = "Assign permissions to a role",
            description = "Assigns one or more permissions to an existing role.")
    ApiResponse<RoleResponse> assignPermissionsToRole(@PathVariable String roleName, @RequestBody PermissionAssignmentRequest request) {
        return ApiResponse.success(roleService.assignPermissions(roleName, request));
    }

    @DeleteMapping("/{roleName}/permissions")
    @Operation(summary = "Revoke permissions from a role",
            description = "Revokes one or more permissions from an existing role.")
    ApiResponse<RoleResponse> revokePermissionsFromRole(@PathVariable String roleName, @RequestBody PermissionAssignmentRequest request) {
        return ApiResponse.success(roleService.revokePermissions(roleName, request));
    }
}
