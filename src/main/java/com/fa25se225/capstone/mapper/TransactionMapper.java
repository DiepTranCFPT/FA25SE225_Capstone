package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.response.TransactionDTO;
import com.fa25se225.capstone.entity.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {
    public TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setStatus(transaction.getStatus() != null ? transaction.getStatus().getName() : null);
        dto.setExternalReference(transaction.getExternalReference());
        dto.setErrorMessage(transaction.getErrorMessage());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }

    public List<TransactionDTO> toDTOList(List<Transaction> transactions) {
        return transactions.stream().map(this::toDTO).collect(Collectors.toList());
    }
}

