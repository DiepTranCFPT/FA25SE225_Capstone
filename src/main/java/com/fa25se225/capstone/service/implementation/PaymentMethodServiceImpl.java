package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.PaymentMethodDTO;
import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.PaymentMethodMapper;
import com.fa25se225.capstone.repository.PaymentMethodRepository;
import com.fa25se225.capstone.service.PaymentMethodService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final AccountUtil accountUtil;

    @Override
    public PaymentMethodDTO createPaymentMethod(String bankingNumber, String nameBanking, String authorName) {
        User teacher = accountUtil.getCurrentUser();
        String expectedAuthorName = (teacher.getFirstName() + " " + teacher.getLastName()).trim();
        if (!expectedAuthorName.equalsIgnoreCase(authorName.trim())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        PaymentMethod paymentMethod = PaymentMethod.builder()
            .teacher(teacher)
            .bankingNumber(bankingNumber)
            .nameBanking(nameBanking)
            .authorName(authorName)
            .build();
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        return PaymentMethodMapper.toDTO(saved);
    }

    @Override
    public List<PaymentMethodDTO> getAllPaymentMethodsByTeacher() {
        User teacher = accountUtil.getCurrentUser();
        return paymentMethodRepository.findAllByTeacher(teacher)
            .stream()
            .map(PaymentMethodMapper::toDTO)
            .collect(Collectors.toList());
    }
}
