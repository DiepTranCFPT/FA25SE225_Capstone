package com.fa25se225.capstone.dto.request;

public record UserSearchRequest(
        String keyword,
        String role,
        Boolean isVerified,
        Boolean isLocked
) {}