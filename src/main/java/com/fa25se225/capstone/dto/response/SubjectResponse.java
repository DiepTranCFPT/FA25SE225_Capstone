package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {
    String id;
    String code;
    String name;
    String description;
    LocalDate createdAt;
    LocalDate updatedAt;
}
