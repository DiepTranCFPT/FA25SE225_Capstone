package com.fa25se225.capstone.service.implementation;


import com.fa25se225.capstone.dto.request.EmailRequest;
import com.fa25se225.capstone.dto.request.SendEmailRequest;
import com.fa25se225.capstone.dto.request.Sender;
import com.fa25se225.capstone.dto.response.EmailResponse;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailService {
    final EmailClient emailClient;

    @Value("${brevo.api-key}")
    String apiKey;

    @Value("${brevo.sender.name}")
    String senderName;

    @Value("${brevo.sender.email}")
    String senderEmail;

    public EmailResponse sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name(senderName)
                        .email(senderEmail)
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e){
            log.error("Feign Exception when sending email: {}", e.contentUTF8(), e);
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }
}