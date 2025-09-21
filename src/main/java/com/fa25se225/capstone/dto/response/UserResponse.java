package com.fa25se225.capstone.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserResponse (
    String id,
    String username,
    String firstName,
    String lastName,
    LocalDate dob,
    Set<RoleResponse> roles
){}
