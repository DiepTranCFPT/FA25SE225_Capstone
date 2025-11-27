package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.PaymentResponse;
import java.util.List;

public interface IPaymentService {
    List<PaymentResponse> getPaymentsByUser();
}

