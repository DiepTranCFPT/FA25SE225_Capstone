package com.fa25se225.capstone.dto.kafka;

import lombok.Builder;

import java.util.Map;

@Builder
public record NotificationEvent(
        String chanel,
        String recipient,
        String templateCode,
        Map<String, Object> params
) {}