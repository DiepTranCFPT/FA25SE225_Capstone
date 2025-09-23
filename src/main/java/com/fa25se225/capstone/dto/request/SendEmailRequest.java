package com.fa25se225.capstone.dto.request;

import lombok.Builder;

@Builder
public record SendEmailRequest(
        Recipient to,
        String subject,
        String htmlContent
) {}
