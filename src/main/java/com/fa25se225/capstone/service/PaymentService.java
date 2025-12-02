package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.PaymentResponse;
import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
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
    public PaymentResponse getPaymentsByUser() {
        User user = accountUtil.getCurrentUser();
        Payment payments = paymentRepository.findByUser(user).orElseThrow(
                ()-> new RuntimeException("Payment not found")
        );
        return paymentMapper.toResponse(payments);
    }
}

