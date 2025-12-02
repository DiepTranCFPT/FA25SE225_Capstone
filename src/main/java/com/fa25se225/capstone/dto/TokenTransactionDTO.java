package com.fa25se225.capstone.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TokenTransactionDTO {
    private String id;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private BigDecimal balanceAfter;
    private TokenTransactionTypeDTO type;
    private String userId;
    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public TokenTransactionTypeDTO getType() { return type; }
    public void setType(TokenTransactionTypeDTO type) { this.type = type; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
