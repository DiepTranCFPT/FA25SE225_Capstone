package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UnlinkStudentRequest(
        @NotBlank
        @Email
        String studentEmail
) {}