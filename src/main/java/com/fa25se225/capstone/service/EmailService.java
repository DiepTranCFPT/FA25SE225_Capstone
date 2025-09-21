package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.EmailResponse;

import java.util.Map;

public interface EmailService {

    EmailResponse sendEmailWithTemplate(String recipientEmail, String templateCode, Map<String, Object> params);
}
