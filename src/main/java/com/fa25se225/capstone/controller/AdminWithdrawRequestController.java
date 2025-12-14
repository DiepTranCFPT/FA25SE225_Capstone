package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.WithdrawRequestDTO;
import com.fa25se225.capstone.service.WithdrawRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/withdraw-requests")
@RequiredArgsConstructor
public class AdminWithdrawRequestController {
    private final WithdrawRequestService withdrawRequestService;

    @GetMapping
    public ResponseEntity<List<WithdrawRequestDTO>> getAllWithdrawRequests() {
        List<WithdrawRequestDTO> requests = withdrawRequestService.getAllWithdrawRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<WithdrawRequestDTO>> getAllWithdrawRequestsNotPending() {
        List<WithdrawRequestDTO> requests = withdrawRequestService.getAllWithdrawRequestsNotPending();
        return ResponseEntity.ok(requests);
    }

}

