package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PermissionRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> getAll();
    void delete(String permission);
    Collection<GrantedAuthority> getAuthoritiesForUser(String userEmail);
    void clearUserPermissionsCache(String userEmail);
    List<PermissionResponse> getPermissionByUserId(String userId);
}
