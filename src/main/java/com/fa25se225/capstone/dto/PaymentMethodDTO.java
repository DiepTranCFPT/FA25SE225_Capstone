package com.fa25se225.capstone.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentMethodDTO {
    private Long id;
    private String bankingNumber;
    private String nameBanking;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
}
