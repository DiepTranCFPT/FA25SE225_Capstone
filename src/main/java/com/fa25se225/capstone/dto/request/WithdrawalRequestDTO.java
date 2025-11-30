package com.fa25se225.capstone.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawalRequestDTO {
    private BigDecimal amount;
    private String description;
}

