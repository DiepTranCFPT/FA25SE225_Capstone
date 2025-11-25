package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.TransactionDTO;
import com.fa25se225.capstone.entity.Transaction;
import com.fa25se225.capstone.service.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final MomoPaymentService momoPaymentService;

    @GetMapping
    public List<TransactionDTO> getCurrentUserTransactions() {
        return momoPaymentService.getCurrentUserTransactions();
    }
}
