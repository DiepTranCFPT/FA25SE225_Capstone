package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.validator.DobConstraint;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;


@Builder
public record UserUpdateRequest (


    String firstName,
    String lastName,

    @DobConstraint(min = 13, max = 105 ,message = "Age must be between 13 and 105 years old")
    LocalDate dob
){}
