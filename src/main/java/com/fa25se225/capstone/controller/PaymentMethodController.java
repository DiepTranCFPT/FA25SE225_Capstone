package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-method")
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<PaymentMethod> createOrUpdatePaymentMethod(
            @RequestParam String bankingNumber,
            @RequestParam String nameBanking) {
        PaymentMethod paymentMethod = paymentMethodService.createOrUpdatePaymentMethod( bankingNumber, nameBanking);
        return ResponseEntity.ok(paymentMethod);
    }

    @GetMapping
    public ResponseEntity<PaymentMethod> getPaymentMethod() {
        return paymentMethodService.getPaymentMethodByTeacher()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

