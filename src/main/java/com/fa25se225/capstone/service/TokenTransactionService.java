package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.entity.TokenTransaction;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TokenTransactionService {
    TokenTransaction requestWithdrawal( WithdrawalRequestDTO dto);
    TokenTransaction confirmWithdrawal(WithdrawalConfirmDTO dto, String adminId);
    void processExamPayment(String studentId, String teacherId, BigDecimal amount, String examTitle);
}

