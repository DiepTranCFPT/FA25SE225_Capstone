package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.PermissionRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}