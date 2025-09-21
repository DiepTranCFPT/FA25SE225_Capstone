package com.fa25se225.capstone.dto.response;


import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import lombok.Builder;

@Builder
public record PermissionResponse (
    String name,
    String description
){}
