package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.TokenTransactionDTO;
import com.fa25se225.capstone.service.TokenTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/token-transaction")
@RequiredArgsConstructor
public class TokenTransactionController {
    private final TokenTransactionService tokenTransactionService;

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<TokenTransactionDTO> requestWithdrawal(@RequestBody WithdrawalRequestDTO dto) {
        return ApiResponse.success(tokenTransactionService.requestWithdrawal(dto));
    }

    @PostMapping("/confirm-withdrawal")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TokenTransactionDTO> confirmWithdrawal(@RequestBody WithdrawalConfirmDTO dto) {
        return ApiResponse.success(tokenTransactionService.confirmWithdrawal(dto));
    }

    @PostMapping("/reject-withdrawal")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TokenTransactionDTO> rejectWithdrawal(@RequestBody WithdrawalConfirmDTO dto) {
        return ApiResponse.success(tokenTransactionService.rejectWithdrawal(dto));
    }

    @GetMapping("/user")
    public ApiResponse<List<TokenTransactionDTO>> getAllByUser() {
        return ApiResponse.success(tokenTransactionService.getAllByUserId());
    }

    @GetMapping("/user/pending-total")
    public ApiResponse<BigDecimal> getPendingTotalByUser() {
        return ApiResponse.success(tokenTransactionService.getPendingTotalByCurrentUser());
    }
}
