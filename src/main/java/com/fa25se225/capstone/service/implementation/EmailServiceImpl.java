package com.fa25se225.capstone.service.implementation;


import com.fa25se225.capstone.dto.request.EmailRequest;
import com.fa25se225.capstone.dto.request.Recipient;
import com.fa25se225.capstone.dto.request.SendEmailRequest;
import com.fa25se225.capstone.dto.request.Sender;
import com.fa25se225.capstone.dto.response.EmailResponse;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.httpclient.EmailClient;
import com.fa25se225.capstone.service.EmailService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailServiceImpl implements EmailService {
    final EmailClient emailClient;


    @Value("${brevo.api-key}")
    String apiKey;
    @Value("${brevo.sender.name}")
    String senderName;
    @Value("${brevo.sender.email}")
    String senderEmail;

//    @Value("${brevo.templates}")
//    String templateId;

    @Override
    public EmailResponse sendEmailWithTemplate(String recipientEmail, String templateCode, Map<String, Object> params) {
        // 1. Tìm templateId từ templateCode
        Long templateId = 1L;
        if (templateId == null) {
            log.error("Template code '{}' not found in configuration.", templateCode);
            throw new AppException(ErrorCode.TEMPLATE_NOT_FOUND); // Tạo một ErrorCode mới
        }

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder().name(senderName).email(senderEmail).build())
                .to(List.of(new Recipient("tri",recipientEmail)))
                .templateId(templateId)
                .params(params)
                .build();

        try {
            log.info("Sending email with template '{}' to {}", templateCode, recipientEmail);
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e) {
            log.error("Feign Exception when sending email with template: {}", e.contentUTF8(), e);
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }
}