package com.fa25se225.capstone.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SubjectResponse(
    String id,
    String code,
    String name,
    String description,
    LocalDate createdAt,
    LocalDate updatedAt
) {}
