package com.fa25se225.capstone.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequestDTO {
    private String transactionId;
    private String teacherId;
    private String teacherName;
    private BigDecimal amount;
    private String status;
    private String bankingNumber;
    private String nameBanking;
    private String createdAt;
}

