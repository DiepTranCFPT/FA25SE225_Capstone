package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.PaymentResponse;
import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.Transaction;
import com.fa25se225.capstone.entity.TransactionStatus;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.PaymentMapper;
import com.fa25se225.capstone.repository.PaymentRepository;
import com.fa25se225.capstone.repository.TransactionRepository;
import com.fa25se225.capstone.repository.TransactionStatusRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.utils.AccountUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final AccountUtil accountUtil;
    private final TransactionRepository transactionRepository;
    private final TransactionStatusRepository transactionStatusRepository;

    @Override
    public PaymentResponse getPaymentsByUser() {
        User user = accountUtil.getCurrentUser();
        Payment payments = paymentRepository.findByUser(user).orElseThrow(
                ()-> new RuntimeException("Payment not found")
        );
        return paymentMapper.toResponse(payments);
    }

    @Override
    @Transactional
    public void transferFromParentToStudent(String parentId, String studentId, Long amount) {
        if (amount == null || amount <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }
        User parent = userRepository.findById(parentId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Payment parentPayment = paymentRepository.findByUser(parent)
            .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        Payment studentPayment = paymentRepository.findByUser(student)
            .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        java.math.BigDecimal transferAmount = java.math.BigDecimal.valueOf(amount);
        if (parentPayment.getAmount().compareTo(transferAmount) < 0) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_TOO_LOW);
        }
        parentPayment.setAmount(parentPayment.getAmount().subtract(transferAmount));
        studentPayment.setAmount(studentPayment.getAmount().add(transferAmount));
        paymentRepository.save(parentPayment);
        paymentRepository.save(studentPayment);

        TransactionStatus successStatus = transactionStatusRepository.findByName("Success").orElse(null);
        LocalDate now = LocalDate.now();
        // Parent transaction (debit)
        Transaction parentTransaction = Transaction.builder()
            .payment(parentPayment)
            .amount(transferAmount.negate())
            .balanceAfter(parentPayment.getAmount())
            .status(successStatus)
            .externalReference("TRANSFER_PARENT_TO_STUDENT "+ student.getFirstName()+" "+student.getLastName())
            .createdAt(now)
            .updatedAt(now)
            .deleted(false)
            .build();
        transactionRepository.save(parentTransaction);
        // Student transaction (credit)
        Transaction studentTransaction = Transaction.builder()
            .payment(studentPayment)
            .amount(transferAmount)
            .balanceAfter(studentPayment.getAmount())
            .status(successStatus)
            .externalReference("TRANSFER_PARENT "+ parent.getFirstName()+" "+parent.getLastName()+" TO_STUDENT")
            .createdAt(now)
            .updatedAt(now)
            .deleted(false)
            .build();
        transactionRepository.save(studentTransaction);
    }
}
