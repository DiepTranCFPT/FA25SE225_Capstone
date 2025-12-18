package com.fa25se225.capstone.dto.kafka;

import lombok.Builder;

@Builder
public record FrqGradingEvent(
        String studentAnswerId,
        String attemptId,
        String modelAnswer,
        String studentAnswerText,
        double maxPoints,

        String questionContent,
        String contextTitle,
        String contextContent
) {}