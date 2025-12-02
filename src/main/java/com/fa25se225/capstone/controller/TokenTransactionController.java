package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.service.TokenTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token-transaction")
@RequiredArgsConstructor
public class TokenTransactionController {
    private final TokenTransactionService tokenTransactionService;

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('TEACHER')")
    public TokenTransaction requestWithdrawal(@RequestBody WithdrawalRequestDTO dto) {
        return tokenTransactionService.requestWithdrawal(dto);
    }

    @PostMapping("/confirm-withdrawal")
    @PreAuthorize("hasRole('ADMIN')")
    public TokenTransaction confirmWithdrawal(@RequestBody WithdrawalConfirmDTO dto) {
        return tokenTransactionService.confirmWithdrawal(dto);
    }

    @PostMapping("/reject-withdrawal")
    @PreAuthorize("hasRole('ADMIN')")
    public TokenTransaction rejectWithdrawal(@RequestBody WithdrawalConfirmDTO dto) {
        return tokenTransactionService.rejectWithdrawal(dto);
    }
}

