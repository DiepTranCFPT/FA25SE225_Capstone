package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserRoleUpdateRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse register(UserCreationRequest request);
    UserResponse getMyProfile();
    PageResponse<List<UserResponse>> getAllUserSortBy(int pageNo, int pageSize, String... sorts);
    UserResponse update(UserUpdateRequest request);
    UserResponse updateUserRole(String id, UserRoleUpdateRequest request);
    void delete(String userId);


}
