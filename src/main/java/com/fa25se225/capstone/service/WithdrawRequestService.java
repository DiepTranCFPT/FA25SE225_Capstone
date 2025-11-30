package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.WithdrawRequestDTO;
import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.PaymentMethod;
import com.fa25se225.capstone.repository.TokenTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawRequestService {
    private final TokenTransactionRepository tokenTransactionRepository;

    public List<WithdrawRequestDTO> getAllWithdrawRequests() {
        List<TokenTransaction> transactions = tokenTransactionRepository.findAll();
        return transactions.stream()
                .filter(tx -> tx.getType().getName().equalsIgnoreCase("withdraw"))
                .map(tx -> {
                    WithdrawRequestDTO dto = new WithdrawRequestDTO();
                    dto.setTransactionId(tx.getId());
                    dto.setTeacherId(tx.getUser().getId());
                    dto.setTeacherName(tx.getUser().getFirstName()+" "+tx.getUser().getLastName());
                    dto.setAmount(tx.getAmount());
                    dto.setStatus(tx.getStatus());
                    PaymentMethod pm = tx.getPaymentMethod();
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

