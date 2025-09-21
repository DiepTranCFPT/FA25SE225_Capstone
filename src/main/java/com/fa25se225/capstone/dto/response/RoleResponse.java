package com.fa25se225.capstone.dto.response;

import lombok.*;

import java.util.Set;


@Builder

public record RoleResponse (
    String name,
    String description,
    Set<PermissionResponse> permissions
){}
