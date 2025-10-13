package com.fa25se225.capstone.mapper.impl;

import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserCreationRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dob(request.dob())
                // password will be set separately with encoding
                .build();
    }

    @Override
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        Set<String> roles = null;
        if (user.getRoles() != null) {
            roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
        }

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getImgUrl(),
                user.getDob(),
                roles
        );
    }

    @Override
    public void updateUser(User user, UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null) {
            return;
        }

        if (userUpdateRequest.firstName() != null) {
            user.setFirstName(userUpdateRequest.firstName());
        }
        if (userUpdateRequest.lastName() != null) {
            user.setLastName(userUpdateRequest.lastName());
        }
        if (userUpdateRequest.dob() != null) {
            user.setDob(userUpdateRequest.dob());
        }
        // password is ignored as specified in mapper
    }
}