package com.fa25se225.capstone.dto.request;

import java.util.List;

public record UserRoleUpdateRequest(
        List<String> roles
) {
}
