package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserRoleUpdateRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;


    @PostMapping
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserCreationRequest request){
        return ApiResponse.success(userService.register(request));
    }

    @GetMapping("/me")
    ApiResponse<UserResponse>getMyInfo(){;
        return ApiResponse.success(userService.getMyProfile());

    }

    @GetMapping
    public ApiResponse<?> getAllUser(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                     @RequestParam(defaultValue = "20", required = false) int pageSize,
                                     @RequestParam(required = false) String... sorts){
        return ApiResponse.success(userService.getAllUserSortBy(pageNo, pageSize, sorts));
    }


    @PutMapping
    public ApiResponse<UserResponse> update(@RequestBody UserUpdateRequest request){
        return ApiResponse.success(userService.update(request));
    }

    @PatchMapping("/{userId}/roles")
    public ApiResponse<UserResponse> updateUserRole(@PathVariable String userId, @RequestBody UserRoleUpdateRequest request){
        return ApiResponse.success(userService.updateUserRole(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<String> delete(@PathVariable String userId){
        userService.delete(userId);
        return ApiResponse.success("Delete successfully");
    }

}
