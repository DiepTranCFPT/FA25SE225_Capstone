package com.fa25se225.capstone.mapper.impl;

import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.dto.response.RoleResponse;
import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.mapper.RoleMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public Role toRole(RoleRequest request) {
        if (request == null) {
            return null;
        }

        return Role.builder()
                .name(request.name())
                .description(request.description())
                // permissions will be set separately
                .build();
    }

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        Set<PermissionResponse> permissions = null;
        if (role.getPermissions() != null) {
            permissions = role.getPermissions().stream()
                    .map(permission -> new PermissionResponse(
                            permission.getName(),
                            permission.getDescription()
                    ))
                    .collect(Collectors.toSet());
        }

        return new RoleResponse(
                role.getName(),
                role.getDescription(),
                permissions
        );
    }
}