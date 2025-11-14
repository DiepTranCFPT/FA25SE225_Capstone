package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.RoleResponse;
import com.fa25se225.capstone.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}