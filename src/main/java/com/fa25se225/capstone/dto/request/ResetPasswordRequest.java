package com.fa25se225.capstone.dto.request;

public record ResetPasswordRequest(String token, String newPassword) {
}
