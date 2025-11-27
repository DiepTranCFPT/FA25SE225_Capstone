package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.PaymentResponse;
import com.fa25se225.capstone.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .userId(payment.getUser().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus() != null ? payment.getStatus().getName() : null)
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

