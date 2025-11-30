package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.entity.TokenTransaction;

public interface TokenTransactionService {
    TokenTransaction requestWithdrawal(String teacherId, WithdrawalRequestDTO dto);
    TokenTransaction confirmWithdrawal(WithdrawalConfirmDTO dto, String adminId);
}

