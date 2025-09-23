package com.fa25se225.capstone.dto.kafka;

import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record NotificationEvent(
        String chanel,
        Set<String> recipients,
        String templateName,
        Map<String, Object> params
) {}