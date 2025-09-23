package com.fa25se225.capstone.dto.request;

import lombok.Builder;

@Builder
public record Sender(
        String name,
        String email
) {}
