package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.EmailResponse;

import java.util.Map;
import java.util.Set;

public interface EmailService {

    EmailResponse sendEmailWithTemplate(Set<String> recipientEmails, String templateCode, Map<String, Object> params);
}
