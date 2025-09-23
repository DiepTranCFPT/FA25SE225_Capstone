package com.fa25se225.capstone.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserResponse (
    String id,
    String email,
    String firstName,
    String lastName,
    String imgUrl,
    LocalDate dob,
    Set<String> roles
){}
