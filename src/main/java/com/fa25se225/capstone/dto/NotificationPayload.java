package com.fa25se225.capstone.dto;

import java.time.Instant;

public record NotificationPayload(
        String id,
        String type,
        String message,
        Instant createdAt,
        boolean isRead
) {}

