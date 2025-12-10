package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.dto.TokenTransactionDTO;

import java.math.BigDecimal;
import java.util.List;

public interface TokenTransactionService {
    TokenTransactionDTO requestWithdrawal(WithdrawalRequestDTO dto);
    TokenTransactionDTO confirmWithdrawal(WithdrawalConfirmDTO dto);
    void processExamPayment(String studentId, String teacherId, BigDecimal amount, String examTitle);
    TokenTransactionDTO rejectWithdrawal(WithdrawalConfirmDTO dto);
    List<TokenTransactionDTO> getAllByUserId();
    BigDecimal getPendingTotalByCurrentUser();
}
