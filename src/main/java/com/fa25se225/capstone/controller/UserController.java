package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserRoleUpdateRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.PermissionService;
import com.fa25se225.capstone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user registration, profile management, and administration")
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Register a new user",
            description = "Creates a new user account. An email verification link will be sent.")
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserCreationRequest request){
        return ApiResponse.success(userService.register(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's profile",
            description = "Retrieves the profile information of the currently authenticated user.")
    ApiResponse<UserResponse>getMyInfo(){;
        return ApiResponse.success(userService.getMyProfile());

    }

    @GetMapping
    @Operation(summary = "Get all users with pagination and sorting (Admin)",
            description = "Retrieves a paginated list of all users. This endpoint is typically for admin use.")
    public ApiResponse<PageResponse<List<UserResponse>>> getAllUser(
            @Parameter(description = "Page number to retrieve (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0", required = false) int pageNo,

            @Parameter(description = "Number of users per page", example = "10")
            @RequestParam(defaultValue = "10", required = false) int pageSize,

            @Parameter(
                    description = "Sorting criteria for the user list. " +
                            "Format: `fieldName:direction`. " +
                            "Multiple sort criteria can be provided. " +
                            "`direction` can be `asc` (ascending) or `desc` (descending).",
                    example = "lastName:asc,createdAt:desc"
            )
            @RequestParam(required = false) String... sorts
    ) {
        return ApiResponse.success(userService.getAllUserSortBy(pageNo, pageSize, sorts));
    }


    @PutMapping("/me")
    @Operation(summary = "Update current user's profile",
            description = "Updates the profile information of the currently authenticated user.")
    public ApiResponse<UserResponse> update(@RequestBody UserUpdateRequest request){
        return ApiResponse.success(userService.update(request));
    }

    @PatchMapping("/{userId}/roles")
    @Operation(summary = "Update a user's roles (Admin)",
            description = "Assigns or updates the roles for a specific user. Requires admin privileges.")
    public ApiResponse<UserResponse> updateUserRole(@PathVariable String userId, @RequestBody UserRoleUpdateRequest request){
        return ApiResponse.success(userService.updateUserRole(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user (Admin)",
            description = "Performs a soft delete on a user by their ID. Requires admin privileges.")
    public ApiResponse<String> delete(@PathVariable String userId){
        userService.delete(userId);
        return ApiResponse.success("Delete successfully");
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload/Update current user's avatar",
            description = "Uploads a new avatar for the authenticated user. If an old avatar exists, it will be replaced.")
    public ApiResponse<UserResponse> updateUserAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateUserAvatar(file));
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Delete current user's avatar",
            description = "Deletes the avatar of the authenticated user.")
    public ApiResponse<UserResponse> deleteUserAvatar() {
        return ApiResponse.success(userService.deleteUserAvatar());
    }


    @PostMapping("/{userId}/permissions/grant")
    @Operation(summary = "Grant permissions for the user",
            description = "Grant permissions for the user by user id.")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> grantPermissionsToUser(@PathVariable String userId, @RequestBody Set<String> permissions) {
        userService.grantPermissions(userId, permissions);
        return ApiResponse.success("Permissions granted successfully.");
    }

    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PermissionResponse>> getUsersPermissions(@PathVariable String userId) {
        return ApiResponse.success(permissionService.getPermissionByUserId(userId));
    }

    @PostMapping("/{userId}/permissions/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke permissions for the user",
            description = "Revoke permissions for the user by user id.")
    public ApiResponse<String> revokePermissionsFromUser(@PathVariable String userId, @RequestBody Set<String> permissions) {
        userService.revokePermissions(userId, permissions);
        return ApiResponse.success("Permissions revoked successfully.");
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile by user ID",
            description = "Retrieves the profile information of a user by their ID, including teacher profile if applicable.")
    public ApiResponse<UserResponse> getProfileByUserId(@PathVariable String userId) {
        return ApiResponse.success(userService.getProfileByUserId(userId));
    }

}
