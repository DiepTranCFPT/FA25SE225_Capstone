package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserRoleUpdateRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface UserService {
    UserResponse register(UserCreationRequest request);
    UserResponse getMyProfile();
    PageResponse<List<UserResponse>> getAllUserSortBy(int pageNo, int pageSize, String... sorts);
    UserResponse update(UserUpdateRequest request);
    UserResponse updateUserRole(String id, UserRoleUpdateRequest request);
    void delete(String userId);
    UserResponse updateUserAvatar(MultipartFile file);
    UserResponse deleteUserAvatar();


    void grantPermissions(String userId, Set<String> permissions);

    void revokePermissions(String userId, Set<String> permissions);

}
