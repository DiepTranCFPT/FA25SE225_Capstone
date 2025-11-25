package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.service.MomoPaymentService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;



@RestController
@RequestMapping("/payment/momo")
@RequiredArgsConstructor
public class MomoPaymentController {
    private static final Logger log = LoggerFactory.getLogger(MomoPaymentController.class);

    private final MomoPaymentService momoPaymentService;
    private final AccountUtil accountUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestParam Long amount) {
        Map<String, Object> response = momoPaymentService.createPaymentRequest( amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/redirect")
    public ResponseEntity<?> handleRedirect(@RequestParam Map<String, String> params) {
        log.info("MOMO REDIRECT PARAMS = {}", params);

        String receivedSignature = params.get("signature");

        boolean valid = momoPaymentService.verifyCallbackSignature(params, receivedSignature);

        if (!valid) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", "Invalid signature", "params", params)
            );
        }
        return ResponseEntity.ok(
                Map.of("status", "success", "message", "Payment verified", "params", params)
        );
    }



    @PostMapping("/ipn")
    public ResponseEntity<?> handleIpn(@RequestBody Map<String, Object> payload) {
        String receivedSignature = (String) payload.get("signature");

        boolean valid = momoPaymentService.verifyCallbackSignature(payload, receivedSignature);

        if (!valid) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", "Invalid signature")
            );
        }

        Object resultCodeObj = payload.get("resultCode");
        int resultCode = resultCodeObj instanceof Number
                ? ((Number) resultCodeObj).intValue()
                : Integer.parseInt(resultCodeObj.toString());

        if (resultCode == 0) {
            String userId = accountUtil.getCurrentUser().getId();
            String orderId = (String) payload.get("orderId");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            String description = (String) payload.get("orderInfo");
            momoPaymentService.handlePaymentSuccess(userId, amount, orderId, description);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment processed"));
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "fail",
                    "message", payload.get("message")
            ));
        }
    }
}
