package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.response.PaymentResponse;
import java.util.List;

public interface IPaymentService {
    PaymentResponse getPaymentsByUser();

    /**
     * Transfer money from parent to student
     */
    void transferFromParentToStudent(String parentId, String studentId, Long amount);
}
