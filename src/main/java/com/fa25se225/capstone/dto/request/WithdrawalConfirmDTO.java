package com.fa25se225.capstone.dto.request;

import lombok.Data;

@Data
public class WithdrawalConfirmDTO {
    private String transactionId;
    private boolean approved;
    private String adminNote;
}

