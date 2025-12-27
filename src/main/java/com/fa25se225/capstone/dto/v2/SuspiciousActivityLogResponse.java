package com.fa25se225.capstone.dto.v2;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SuspiciousActivityLogResponse {
    private String message;
    private LocalDateTime createdAt;
}
