package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.RoleResponse;
import com.fa25se225.capstone.entity.Role;
// import org.mapstruct.Mapper;
// import org.mapstruct.Mapping;

// @Mapper(componentModel = "spring")
public interface RoleMapper {
    // @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}