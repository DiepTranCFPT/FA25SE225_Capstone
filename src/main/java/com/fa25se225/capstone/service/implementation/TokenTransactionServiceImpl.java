package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.WithdrawalRequestDTO;
import com.fa25se225.capstone.dto.request.WithdrawalConfirmDTO;
import com.fa25se225.capstone.dto.TokenTransactionDTO;
import com.fa25se225.capstone.dto.TokenTransactionTypeDTO;
import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.*;
import com.fa25se225.capstone.service.TokenTransactionService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenTransactionServiceImpl implements TokenTransactionService {
    private final UserRepository userRepository;
    private final TokenTransactionRepository tokenTransactionRepository;
    private final TokenTransactionTypeRepository tokenTransactionTypeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final AccountUtil accountUtil;
    private static final BigDecimal TEACHER_SHARE_PERCENTAGE = BigDecimal.valueOf(0.8);

    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_FAIL = "fail";
    private static final String STATUS_REJECT = "reject";



    @Override
    @Transactional
    public TokenTransactionDTO requestWithdrawal(WithdrawalRequestDTO dto) {
        boolean check =  checkDupTransaction();
        if(!check){
            throw new AppException(ErrorCode.TRANSACTION_IS_VALID);
        }
        User teacher = accountUtil.getCurrentUser();
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
                .status(STATUS_PENDING)
                .deleted(false)
                .build();
        tokenTransactionRepository.save(transaction);
        return toDTO(transaction);
    }

    @Override
    @Transactional
    public TokenTransactionDTO confirmWithdrawal(WithdrawalConfirmDTO dto) {
        TokenTransaction transaction = tokenTransactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        if (!transaction.getStatus().equals(STATUS_PENDING)) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        if (dto.isApproved()) {
            transaction.setStatus(STATUS_SUCCESS);
            User teacher = transaction.getUser();
           Payment payment =  paymentRepository.findByUser(teacher).get();
           payment.setAmount(transaction.getBalanceAfter());
           paymentRepository.saveAndFlush(payment);
        } else {
            transaction.setStatus(STATUS_PENDING);
        }
        transaction.setDescription(transaction.getDescription() + " | Admin note: " + dto.getAdminNote());
        tokenTransactionRepository.save(transaction);
        return toDTO(transaction);
    }

    @Override
    public TokenTransactionDTO rejectWithdrawal(WithdrawalConfirmDTO dto) {
        TokenTransaction transaction = tokenTransactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        if (!transaction.getStatus().equals(STATUS_PENDING)) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        if (dto.isApproved()) {
            transaction.setStatus(STATUS_REJECT);
        } else {
            transaction.setStatus(STATUS_PENDING);
        }
        transaction.setDescription(transaction.getDescription() + " | Admin note: " + dto.getAdminNote());
        tokenTransactionRepository.save(transaction);
        return toDTO(transaction);
    }

    @Override
    @Transactional
    public void processExamPayment(String studentId, String teacherId, BigDecimal amount, String examTitle) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        PaymentStatus paymentStatus = paymentStatusRepository.findByCode("active").orElse(null);

        Payment studentPayment = paymentRepository.findByUser(student).orElseGet(() ->
                {
                    Payment newPayment = Payment.builder()
                            .user(student)
                            .amount(BigDecimal.ZERO)
                            .createdAt(LocalDate.now())
                            .status(paymentStatus)
                            .updatedAt(LocalDate.now()).build();
                    return paymentRepository.save(newPayment);
                }
        );

        Payment teacherPayment = paymentRepository.findByUser(teacher).orElseGet(() ->
                {
                    Payment newPayment = Payment.builder()
                            .user(teacher)
                            .amount(BigDecimal.ZERO)
                            .createdAt(LocalDate.now())
                            .status(paymentStatus)
                            .updatedAt(LocalDate.now()).build();
                    return paymentRepository.save(newPayment);
                }
        );

        if (studentPayment.getAmount().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal teacherIncome =  amount.multiply(TEACHER_SHARE_PERCENTAGE);

        studentPayment.setAmount(studentPayment.getAmount().subtract(amount));
        paymentRepository.save(studentPayment);

        TokenTransactionType examPayment = tokenTransactionTypeRepository.findByName("EXAM_PAYMENT")
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_TYPE_NOT_FOUND));

        TokenTransactionType incomeShare = tokenTransactionTypeRepository.findByName("INCOME_SHARE")
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_TYPE_NOT_FOUND));

        createTransaction(student, amount.negate(), examPayment, "Payment for exam: " + examTitle);

        teacherPayment.setAmount(teacherPayment.getAmount().add(teacherIncome));
        paymentRepository.save(teacherPayment);

        createTransaction(teacher, teacherIncome, incomeShare, "Revenue from exam: " + examTitle);


    }

    @Override
    public List<TokenTransactionDTO> getAllByUserId() {
        User user = accountUtil.getCurrentUser();
        List<TokenTransaction> transactions = tokenTransactionRepository.findAllByUser(user);
        return transactions.stream().map(this::toDTO).toList();
    }

    private TokenTransactionDTO toDTO(TokenTransaction entity) {
        TokenTransactionDTO dto = new TokenTransactionDTO();
        dto.setId(entity.getId());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setBalanceAfter(entity.getBalanceAfter());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        if (entity.getType() != null) {
            TokenTransactionTypeDTO typeDTO = new TokenTransactionTypeDTO();
            typeDTO.setId(entity.getType().getId());
            typeDTO.setName(entity.getType().getName());
            typeDTO.setDescription(entity.getType().getDescription());
            dto.setType(typeDTO);
        }
        return dto;
    }

    private void createTransaction(User user, BigDecimal amount, TokenTransactionType type, String description) {
        TokenTransaction tx = TokenTransaction.builder()
                .user(user)
                .amount(amount)
                .type(type)
                .description(description)
                .createdAt(LocalDate.now())
                .status(STATUS_SUCCESS)
                .build();
        tokenTransactionRepository.save(tx);
    }
    private Boolean checkDupTransaction(){
        long count = 0;
        User teacher = accountUtil.getCurrentUser();
        List<TokenTransaction> list = tokenTransactionRepository.findAllByUser(teacher);
        count =  list.stream()
                .filter(t -> t.getStatus().equals(STATUS_PENDING)).count();
        return count == 0;

    }
}
