package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.Min;

public record ChangePasswordRequest(String currentPassword, @Min(8) String newPassword, String token) {
}
