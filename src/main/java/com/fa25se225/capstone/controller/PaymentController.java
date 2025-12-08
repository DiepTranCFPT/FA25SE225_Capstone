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

    @PostMapping("/transfer-parent-to-student")
    public ApiResponse<?> transferParentToStudent(@RequestParam String parentId, @RequestParam String studentId, @RequestParam Long amount) {
        paymentService.transferFromParentToStudent(parentId, studentId, amount);
        return ApiResponse.success("Transfer successful");
    }
}
