package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.PaymentMethodDTO;
import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-method")
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<PaymentMethodDTO> createPaymentMethod(
            @RequestParam String bankingNumber,
            @RequestParam String nameBanking,
            @RequestParam String authorName) {
        PaymentMethodDTO paymentMethod = paymentMethodService.createPaymentMethod(bankingNumber, nameBanking, authorName);
        return ResponseEntity.ok(paymentMethod);
    }

    @GetMapping
    public ResponseEntity<java.util.List<PaymentMethodDTO>> getAllPaymentMethods() {
        return ResponseEntity.ok(paymentMethodService.getAllPaymentMethodsByTeacher());
    }
}
