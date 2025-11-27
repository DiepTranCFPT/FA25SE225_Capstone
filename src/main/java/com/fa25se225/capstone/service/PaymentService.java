package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.PaymentResponse;
import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.mapper.PaymentMapper;
import com.fa25se225.capstone.repository.PaymentRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final AccountUtil accountUtil;

    @Override
    public List<PaymentResponse> getPaymentsByUser() {
        User user = accountUtil.getCurrentUser();
        if (user == null) return List.of();
        List<Payment> payments = paymentRepository.findAllByUser(user);
        return payments.stream().map(paymentMapper::toResponse).collect(Collectors.toList());
    }
}

