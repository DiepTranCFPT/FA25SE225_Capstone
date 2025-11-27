package com.fa25se225.capstone.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String paymentNumber;
    private String userId;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}

