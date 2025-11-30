package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.TokenTransactionType;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.PaymentRepository;
import com.fa25se225.capstone.repository.TokenTransactionRepository;
import com.fa25se225.capstone.repository.TokenTransactionTypeRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.TokenTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TokenTransactionServiceImpl implements TokenTransactionService {
    private final UserRepository userRepository;
    private final TokenTransactionRepository tokenTransactionRepository;
    private final TokenTransactionTypeRepository tokenTransactionTypeRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public TokenTransaction requestWithdrawal(String teacherId, WithdrawalRequestDTO dto) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }
        if (paymentRepository.findByUser(teacher).isEmpty() || paymentRepository.findByUser(teacher).get().getAmount().compareTo(dto.getAmount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        TokenTransactionType type = tokenTransactionTypeRepository.findByName("WITHDRAWAL")
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_TYPE_NOT_FOUND));
        TokenTransaction transaction = TokenTransaction.builder()
                .user(teacher)
                .amount(dto.getAmount())
                .type(type)
                .description(dto.getDescription())
                .balanceAfter(paymentRepository.findByUser(teacher).get().getAmount().subtract(dto.getAmount()))
                .createdAt(LocalDate.now())
                .status("pending")
                .deleted(false)
                .build();
        tokenTransactionRepository.save(transaction);
        return transaction;
    }

    @Override
    @Transactional
    public TokenTransaction confirmWithdrawal(WithdrawalConfirmDTO dto, String adminId) {
        TokenTransaction transaction = tokenTransactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        if (!transaction.getStatus().equals("pending")) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        if (dto.isApproved()) {
            transaction.setStatus("success");
            User teacher = transaction.getUser();
           Payment payment =  paymentRepository.findByUser(teacher).get();
           payment.setAmount(transaction.getBalanceAfter());
           paymentRepository.saveAndFlush(payment);
        } else {
            transaction.setStatus("fail");
        }
        transaction.setDescription(transaction.getDescription() + " | Admin note: " + dto.getAdminNote());
        tokenTransactionRepository.save(transaction);
        return transaction;
    }
}

