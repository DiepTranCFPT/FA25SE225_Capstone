package com.fa25se225.capstone.mapper.impl;

import com.fa25se225.capstone.dto.request.PermissionRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.entity.Permission;
import com.fa25se225.capstone.mapper.PermissionMapper;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission toPermission(PermissionRequest request) {
        if (request == null) {
            return null;
        }

        return Permission.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    @Override
    public PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return new PermissionResponse(
                permission.getName(),
                permission.getDescription()
        );
    }
}