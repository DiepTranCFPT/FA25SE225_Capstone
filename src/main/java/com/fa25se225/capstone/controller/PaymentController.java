package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.PaymentResponse;
import com.fa25se225.capstone.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final IPaymentService paymentService;

    @GetMapping("/by-user")
    public ApiResponse<PaymentResponse> getPaymentsByUser() {
        PaymentResponse payments = paymentService.getPaymentsByUser();
        return ApiResponse.success(payments);
    }
}

