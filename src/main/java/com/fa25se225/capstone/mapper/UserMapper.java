package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "roles", ignore = true)
    UserResponse toResponse(User user);

    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);

}