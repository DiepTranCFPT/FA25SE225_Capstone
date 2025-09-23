package com.fa25se225.capstone.dto.request;

import java.util.Set;

public record RoleRequest(
        String name,
        String description,
        Set<String> permissions
) {}
