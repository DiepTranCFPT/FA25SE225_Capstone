package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.PaymentMethodDTO;

import java.util.List;

public interface PaymentMethodService {
    PaymentMethodDTO createPaymentMethod(String bankingNumber, String nameBanking, String authorName);
    List<PaymentMethodDTO> getAllPaymentMethodsByTeacher();
}
