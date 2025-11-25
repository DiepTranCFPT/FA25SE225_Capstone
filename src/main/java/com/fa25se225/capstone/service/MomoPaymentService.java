package com.fa25se225.capstone.service;

import com.fa25se225.capstone.configuration.properties.MomoConfig;
import com.fa25se225.capstone.entity.Payment;
import com.fa25se225.capstone.entity.PaymentStatus;
import com.fa25se225.capstone.entity.Transaction;
import com.fa25se225.capstone.entity.TransactionStatus;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.PaymentRepository;
import com.fa25se225.capstone.repository.PaymentStatusRepository;
import com.fa25se225.capstone.repository.TokenTransactionRepository;
import com.fa25se225.capstone.repository.TransactionRepository;
import com.fa25se225.capstone.repository.TransactionStatusRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentService {

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionStatusRepository transactionStatusRepository;
    private final AccountUtil accountUtil;
    private final TransactionRepository transactionRepository;
    private final PaymentStatusRepository paymentStatusRepository;


    @Transactional
    public Map<String, Object> createPaymentRequest(Long amount) {
        String extraData = "";
        String requestType = "captureWallet";
        String oderInfo = "TOP UP WALLET";
        User user = accountUtil.getCurrentUser();
        PaymentStatus paymemtStatus = paymentStatusRepository.findByCode("active").orElse(null);
        Payment payment = paymentRepository.findByUser(user).orElseGet(() ->
                {
                    Payment newPayment = Payment.builder()
                            .user(user)
                            .amount(BigDecimal.ZERO)
                            .createdAt(LocalDate.now())
                            .status(paymemtStatus)
                            .updatedAt(LocalDate.now()).build();
                    return paymentRepository.save(newPayment);
                }
        );
        TransactionStatus transactionStatus = transactionStatusRepository.findByName("Pending").orElseThrow(() ->
                new RuntimeException("NOT FOUND"));
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal(amount))
                .balanceAfter(payment.getAmount())
                .status(transactionStatus)
                .payment(payment)
                .externalReference(oderInfo)
                .build();
        transactionRepository.saveAndFlush(transaction);

        Map<String, Object> payload = new HashMap<>();
        payload.put("partnerCode", momoConfig.getPartnerCode());
        payload.put("accessKey", momoConfig.getAccessKey());
        payload.put("requestId",transaction.getId());
        payload.put("amount", amount);
        payload.put("orderId", transaction.getId());
        payload.put("orderInfo", oderInfo);
        payload.put("redirectUrl", momoConfig.getRedirectUrl());
        payload.put("ipnUrl", momoConfig.getIpnUrl());
        payload.put("lang", "en");
        payload.put("extraData", extraData);
        payload.put("requestType", requestType);

        String rawSignature =
                "accessKey=" + momoConfig.getAccessKey()
                        + "&amount=" + amount
                        + "&extraData=" + extraData
                        + "&ipnUrl=" + momoConfig.getIpnUrl()
                        + "&orderId=" + transaction.getId()
                        + "&orderInfo=" + oderInfo
                        + "&partnerCode=" + momoConfig.getPartnerCode()
                        + "&redirectUrl=" + momoConfig.getRedirectUrl()
                        + "&requestId=" + transaction.getId()
                        + "&requestType=" + requestType;

        log.info("MoMo rawSignature /create = {}", rawSignature);

        String signature = hmacSHA256(rawSignature, momoConfig.getSecretKey());
        payload.put("signature", signature);

        log.info("MoMo signature /create = {}", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(momoConfig.getEndpoint(), request, Map.class);

        return response.getBody();
    }

    @Transactional
    public void handlePaymentSuccess(String userId,
                                     BigDecimal amount,
                                     String orderId,
                                     String description) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.error("User not found for payment success: {}", userId);
            return;
        }
        User user = userOpt.get();

        TransactionStatus transactionStatus = transactionStatusRepository.findByName("Success").orElseThrow(() ->
                new RuntimeException("NOT FOUND STATUS"));

        Transaction transaction = transactionRepository.findById(orderId).orElseThrow(()->
                new RuntimeException("NOT FOUND TRANSACTION"));
        if(transaction.getStatus().getName().equals("Success")) {
            new RuntimeException("Transaction have been successful");
        }
        transaction.setStatus(transactionStatus);
        transaction.setExternalReference(description);
        transactionRepository.save(transaction);

        Payment payment = paymentRepository.findByUser(user).orElseGet(() ->
                {
                    Payment newPayment = Payment.builder()
                            .user(user)
                            .amount(BigDecimal.ZERO)
                            .createdAt(LocalDate.now())
                            .updatedAt(LocalDate.now()).build();
                    return paymentRepository.save(newPayment);
                }
        );
        BigDecimal newBalance = payment.getAmount().add(amount);
        payment.setAmount(newBalance);
        paymentRepository.save(payment);

        log.info("Payment success handled: user={}, amount={}, orderId={}", userId, amount, orderId);
    }

    private String hmacSHA256(String data, String secretKey) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error generating Momo signature: {}", e.getMessage(), e);
            return "";
        }
    }

    private String v(Object value) {
        return value == null ? "" : value.toString();
    }

    public boolean verifyCallbackSignature(Map<String, ?> data, String receivedSignature) {
        try {
            String rawSignature =
                    "accessKey=" + momoConfig.getAccessKey()
                            + "&amount=" + v(data.get("amount"))
                            + "&extraData=" + v(data.get("extraData"))
                            + "&message=" + v(data.get("message"))
                            + "&orderId=" + v(data.get("orderId"))
                            + "&orderInfo=" + v(data.get("orderInfo"))
                            + "&orderType=" + v(data.get("orderType"))
                            + "&partnerCode=" + v(data.get("partnerCode"))
                            + "&payType=" + v(data.get("payType"))
                            + "&requestId=" + v(data.get("requestId"))
                            + "&responseTime=" + v(data.get("responseTime"))
                            + "&resultCode=" + v(data.get("resultCode"))
                            + "&transId=" + v(data.get("transId"));

            log.info("MoMo rawSignature callback = {}", rawSignature);

            String expected = hmacSHA256(rawSignature, momoConfig.getSecretKey());

            log.info("MoMo expected signature = {}", expected);
            log.info("MoMo received signature = {}", receivedSignature);

            return expected.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Error verifying Momo callback signature: {}", e.getMessage(), e);
            return false;
        }
    }
}
