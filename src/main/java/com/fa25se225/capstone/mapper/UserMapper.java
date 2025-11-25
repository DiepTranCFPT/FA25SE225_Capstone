package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.ParentProfileResponse;
import com.fa25se225.capstone.dto.response.StudentProfileResponse;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.*;

import java.util.Set;
import java.util.stream.Collectors;

import com.fa25se225.capstone.mapper.v2.QuestionV2Mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {StudentProfileMapper.class, TeacherProfileMapper.class, ParentProfileMapper.class})
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);

    UserResponse toResponse(User user);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.imgUrl", target = "imgUrl")
    @Mapping(source = "user.dob", target = "dob")
    @Mapping(source = "user.roles", target = "roles")
    @Mapping(source = "teacherProfile", target = "teacherProfile")
    @Mapping(source = "parentProfile", target = "parentProfile")
    @Mapping(source = "studentProfile", target = "studentProfile")
    UserResponse toResponse(User user, TeacherProfile teacherProfile, ParentProfile parentProfile, StudentProfile studentProfile);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.imgUrl", target = "imgUrl")
    @Mapping(source = "user.dob", target = "dob")
    @Mapping(source = "user.roles", target = "roles")
    @Mapping(source = "teacherProfile", target = "teacherProfile")
    UserResponse toResponse(User user, TeacherProfileResponse teacherProfile);

    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}