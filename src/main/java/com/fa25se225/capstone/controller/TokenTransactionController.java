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
    public TokenTransaction requestWithdrawal(@RequestBody WithdrawalRequestDTO dto, @RequestParam String teacherId) {
        return tokenTransactionService.requestWithdrawal(teacherId, dto);
    }

    @PostMapping("/confirm-withdrawal")
    @PreAuthorize("hasRole('ADMIN')")
    public TokenTransaction confirmWithdrawal(@RequestBody WithdrawalConfirmDTO dto, @RequestParam String adminId) {
        return tokenTransactionService.confirmWithdrawal(dto, adminId);
    }
}

