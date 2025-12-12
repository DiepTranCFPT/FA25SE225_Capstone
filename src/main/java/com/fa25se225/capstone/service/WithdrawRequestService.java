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
    private final String status_pending = "pending";
    private final String WITHDRAWAL = "WITHDRAWAL";

    public List<WithdrawRequestDTO> getAllWithdrawRequests() {
        List<TokenTransaction> transactions = tokenTransactionRepository.findAll();
        return transactions.stream()
                .filter(tx -> tx.getType().getName().equalsIgnoreCase(WITHDRAWAL))
                .filter(tx -> tx.getStatus().equalsIgnoreCase(status_pending))
                .map(tx -> {
                    WithdrawRequestDTO dto = new WithdrawRequestDTO();
                    dto.setTransactionId(tx.getId());
                    dto.setTeacherId(tx.getUser().getId());
                    dto.setTeacherName(tx.getUser().getFirstName()+" "+tx.getUser().getLastName());
                    dto.setAmount(tx.getAmount());
                    dto.setStatus(tx.getStatus());
                    List<PaymentMethod> paymentMethods = paymentMethodRepository.findAllByTeacher(tx.getUser());
                    if (paymentMethods.isEmpty()) {
                        throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                    }
                    // Use the most recently created payment method
                    PaymentMethod pm = paymentMethods.stream()
                        .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                        .orElse(paymentMethods.get(0));
                    dto.setBankingNumber(pm.getBankingNumber());
                    dto.setNameBanking(pm.getNameBanking());
                    dto.setAuthorName(pm.getAuthorName());
                    dto.setCreatedAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
