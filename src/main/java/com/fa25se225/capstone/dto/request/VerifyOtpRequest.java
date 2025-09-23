package com.fa25se225.capstone.dto.request;

import lombok.Builder;

@Builder
public record VerifyOtpRequest(String email, String otp, String newPassword) {
}
