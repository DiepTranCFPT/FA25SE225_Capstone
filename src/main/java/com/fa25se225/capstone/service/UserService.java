package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.AdminUnverifiedTeacherResponse;
import com.fa25se225.capstone.dto.response.PageResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface UserService {
    UserResponse getMyProfile();
    PageResponse<List<UserResponse>> getAllUserSortBy(int pageNo, int pageSize, String... sorts);
    UserResponse update(UserUpdateRequest request);
    UserResponse updateUserRole(String id, UserRoleUpdateRequest request);
    void delete(String userId);
    UserResponse updateUserAvatar(MultipartFile file);
    UserResponse deleteUserAvatar();

    void grantPermissions(String userId, Set<String> permissions);

    void revokePermissions(String userId, Set<String> permissions);

    List<UserResponse> getUnverifiedTeachers();

    UserResponse verifyTeacher(String userId);

    UserResponse getProfileByUserId(String userId);

    User createUser(UserCreationRequest request);

    PageResponse<List<UserResponse>> searchUsers(UserSearchRequest request, int pageNo, int pageSize, String... sorts);

    PageResponse<List<UserResponse>> getAllUsersHaveTeacherRole(int pageNo, int pageSize, String[] sorts);

    List<AdminUnverifiedTeacherResponse> getUnverifiedTeachersForAdmin() ;
}
