package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.PermissionAssignmentRequest;
import com.fa25se225.capstone.dto.request.RoleRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.dto.response.RoleResponse;
import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.PermissionMapper;
import com.fa25se225.capstone.mapper.RoleMapper;
import com.fa25se225.capstone.repository.PermissionRepository;
import com.fa25se225.capstone.repository.RoleRepository;
import com.fa25se225.capstone.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;
    PermissionMapper permissionMapper;


    @Override
    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsById(request.name())) {
            throw new AppException(ErrorCode.EXISTED_ROLE);
        }
        Role role = roleMapper.toRole(request);

        if (request.permissions() != null && !request.permissions().isEmpty()) {
            var permissions = permissionRepository.findAllById(request.permissions());
            role.setPermissions(new HashSet<>(permissions));
        }

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void delete(String roleName) {
        findRoleByNameOrThrowException(roleName);
        roleRepository.deleteById(roleName);

    }

    @Override
    public List<PermissionResponse> getPermissions(String roleName) {
        Role role = findRoleByNameOrThrowException(roleName);
        return role.getPermissions().stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    @Transactional
    public RoleResponse assignPermissions(String roleName, PermissionAssignmentRequest request) {
        Role role = findRoleByNameOrThrowException(roleName);
        var permissionsToAdd = permissionRepository.findAllById(request.permissionNames());
        if(permissionsToAdd.isEmpty()){
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        role.getPermissions().addAll(permissionsToAdd);
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @Override
    public RoleResponse revokePermissions(String roleName, PermissionAssignmentRequest request) {
        Role role = findRoleByNameOrThrowException(roleName);
        var permissionsToRevoke = permissionRepository.findAllById(request.permissionNames());
        if(permissionsToRevoke.isEmpty()){
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        role.getPermissions().removeAll(permissionsToRevoke);
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    private Role findRoleByNameOrThrowException(String roleName){
        return roleRepository.findById(roleName.toUpperCase()).orElseThrow(() -> new AppException(ErrorCode.INVALID_ROLE_NAME));
    }
}
