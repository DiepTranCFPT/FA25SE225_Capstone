package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.PaymentMethodRepository;
import com.fa25se225.capstone.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public PaymentMethod createOrUpdatePaymentMethod(User teacher, String bankingNumber, String nameBanking) {
        Optional<PaymentMethod> existing = paymentMethodRepository.findByTeacher(teacher);
        PaymentMethod paymentMethod = existing.orElse(PaymentMethod.builder().teacher(teacher).build());
        paymentMethod.setBankingNumber(bankingNumber);
        paymentMethod.setNameBanking(nameBanking);
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public Optional<PaymentMethod> getPaymentMethodByTeacher(User teacher) {
        return paymentMethodRepository.findByTeacher(teacher);
    }
}

