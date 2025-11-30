package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.PaymentMethodRepository;
import com.fa25se225.capstone.service.PaymentMethodService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final AccountUtil accountUtil;

    @Override
    public PaymentMethod createOrUpdatePaymentMethod( String bankingNumber, String nameBanking) {
        User teacher = accountUtil.getCurrentUser();
        Optional<PaymentMethod> existing = paymentMethodRepository.findByTeacher(teacher);
        PaymentMethod paymentMethod = existing.orElse(PaymentMethod.builder().teacher(teacher).build());
        paymentMethod.setBankingNumber(bankingNumber);
        paymentMethod.setNameBanking(nameBanking);
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public Optional<PaymentMethod> getPaymentMethodByTeacher() {
        User teacher = accountUtil.getCurrentUser();
        return paymentMethodRepository.findByTeacher(teacher);
    }
}

