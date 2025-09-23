package com.fa25se225.capstone.service.implementation;


import com.fa25se225.capstone.configuration.properties.BrevoProperties;
import com.fa25se225.capstone.dto.request.EmailRequest;
import com.fa25se225.capstone.dto.request.Recipient;
import com.fa25se225.capstone.dto.request.Sender;
import com.fa25se225.capstone.dto.response.EmailResponse;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.httpclient.EmailClient;
import com.fa25se225.capstone.service.EmailService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailClient emailClient;
    private final BrevoProperties brevoProperties;

    @Override
    public EmailResponse sendEmailWithTemplate(Set<String> recipientEmails, String templateName, Map<String, Object> params) {

        if (recipientEmails == null || recipientEmails.isEmpty()) {
            return null;
        }

        Long templateId = brevoProperties.getTemplates().get(templateName);
        if (templateId == null) {
            throw new AppException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        String defaultRecipientName = params.getOrDefault("firstName", "User").toString();

        List<Recipient> listRecipients = recipientEmails.stream()
                .map(email -> new Recipient(defaultRecipientName, email))
                .toList();

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name(brevoProperties.getSender().getName())
                        .email(brevoProperties.getSender().getEmail())
                        .build())
                .to(listRecipients)
                .templateId(templateId)
                .params(params)
                .build();

        try {
            log.info("Sending email with templateId '{}' (code: '{}') to {} recipients", templateId, templateName, listRecipients.size());
            return emailClient.sendEmail(brevoProperties.getApiKey(), emailRequest);
        } catch (FeignException e) {
            log.error("Feign Exception when sending email with template: {}", e.contentUTF8(), e);
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }
}