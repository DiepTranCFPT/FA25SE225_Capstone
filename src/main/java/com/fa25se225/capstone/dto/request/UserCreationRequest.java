package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.validator.DobConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UserCreationRequest(

    @Size(min = 4, message = "Invalid Email")
    String email,

    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotBlank(message = "First name must be not empty")
    String firstName,

    @NotBlank(message = "Last name must be not empty")
    String lastName,

    @NotNull(message = "Date of birth must not be null")
    @DobConstraint(min = 13, message = "At least 13 years old")
    LocalDate dob,

    @NotBlank(message = "You must choose the role")
    String roleName


) {}
