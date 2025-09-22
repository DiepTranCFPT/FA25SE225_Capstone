package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PermissionAssignmentRequest;
import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest request);

    List<RoleResponse> getAll();

    void delete(String roleName);

    List<PermissionResponse> getPermissions(String roleName);

    RoleResponse assignPermissions(String roleName, PermissionAssignmentRequest request);

    RoleResponse revokePermissions(String roleName, PermissionAssignmentRequest request);
}
