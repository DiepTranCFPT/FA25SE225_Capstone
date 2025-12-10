package com.fa25se225.capstone.service;

import com.fa25se225.capstone.entity.PaymentMethod;

import java.util.Optional;

public interface PaymentMethodService {
    PaymentMethod createOrUpdatePaymentMethod( String bankingNumber, String nameBanking);
    PaymentMethod getPaymentMethodByTeacher();
}
