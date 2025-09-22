package com.fa25se225.capstone.dto.request;

public record ChangePasswordRequest(String currentPassword, String newPassword, String token) {
}
