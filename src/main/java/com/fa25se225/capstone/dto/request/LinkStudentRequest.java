package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LinkStudentRequest(
        @NotBlank
        @Email
        String studentEmail,

        @NotBlank(message = "Connection code is required")
        String connectionCode
        ) {}