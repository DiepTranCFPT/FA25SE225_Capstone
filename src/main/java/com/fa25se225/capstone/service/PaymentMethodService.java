package com.fa25se225.capstone.service;

import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.entity.User;
import java.util.Optional;

public interface PaymentMethodService {
    PaymentMethod createOrUpdatePaymentMethod(User teacher, String bankingNumber, String nameBanking);
    Optional<PaymentMethod> getPaymentMethodByTeacher(User teacher);
}
