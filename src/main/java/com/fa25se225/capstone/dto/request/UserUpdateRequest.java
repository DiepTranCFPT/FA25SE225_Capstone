package com.fa25se225.capstone.dto.request;

import com.fa25se225.capstone.validator.DobConstraint;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;


@Builder
public record UserUpdateRequest (

    @Min(8)
    String password,
    String firstName,
    String lastName,
    String imgUrl,

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob
){}
