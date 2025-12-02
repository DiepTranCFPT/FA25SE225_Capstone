package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.WithdrawRequestDTO;
import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.PaymentMethodRepository;
import com.fa25se225.capstone.repository.TokenTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawRequestService {
    private final TokenTransactionRepository tokenTransactionRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public List<WithdrawRequestDTO> getAllWithdrawRequests() {
        List<TokenTransaction> transactions = tokenTransactionRepository.findAll();
        return transactions.stream()
                .filter(tx -> tx.getType().getName().equalsIgnoreCase("WITHDRAWAL"))
                .map(tx -> {
                    WithdrawRequestDTO dto = new WithdrawRequestDTO();
                    dto.setTransactionId(tx.getId());
                    dto.setTeacherId(tx.getUser().getId());
                    dto.setTeacherName(tx.getUser().getFirstName()+" "+tx.getUser().getLastName());
                    dto.setAmount(tx.getAmount());
                    dto.setStatus(tx.getStatus());
                    PaymentMethod pm = paymentMethodRepository.findByTeacher(tx.getUser()).orElseThrow(
                            ()-> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
                    if (pm != null) {
                        dto.setBankingNumber(pm.getBankingNumber());
                        dto.setNameBanking(pm.getNameBanking());
                    }
                    dto.setCreatedAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}

