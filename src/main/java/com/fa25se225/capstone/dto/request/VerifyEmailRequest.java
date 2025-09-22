package com.fa25se225.capstone.dto.request;

import lombok.Builder;

@Builder
public record VerifyEmailRequest(String email, String token) {
}
