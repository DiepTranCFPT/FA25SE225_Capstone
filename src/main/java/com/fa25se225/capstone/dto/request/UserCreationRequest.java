package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.validator.DobConstraint;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreationRequest(
    @Size(min = 4, message = "USERNAME_INVALID") String username,
    @Size(min = 6, message = "INVALID_PASSWORD") String password,
    String firstName,
    String lastName,
    @DobConstraint(min = 10, message = "INVALID_DOB") LocalDate dob
) {}
