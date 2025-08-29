package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class PaymentGatewayService {

    private final Random random = new Random();

    public Map<String, Object> processPayment(Payment payment) {
        log.info("Processing payment through gateway for amount: {} {}", payment.getAmount(), payment.getCurrency());

        // Simulate payment gateway processing
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Simulate processing delay
            Thread.sleep(random.nextInt(500) + 500);

            // Simulate different payment scenarios
            double successRate = 0.85; // 85% success rate
            boolean isSuccessful = random.nextDouble() <= successRate;

            if (isSuccessful) {
                response.put("success", true);
                response.put("gatewayReference", "GW_" + UUID.randomUUID().toString());
                response.put("authorizationCode", generateAuthCode());
                response.put("message", "Payment processed successfully");
                
                // Add card details if available
                if (payment.getPaymentMethod() != null && payment.getPaymentMethod().startsWith("card")) {
                    response.put("cardLastFour", generateCardLastFour());
                    response.put("cardBrand", detectCardBrand());
                }
            } else {
                response.put("success", false);
                response.put("errorCode", generateErrorCode());
                response.put("message", generateFailureReason());
            }

            response.put("timestamp", System.currentTimeMillis());
            response.put("processingTime", random.nextInt(300) + 100);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.put("success", false);
            response.put("errorCode", "GATEWAY_ERROR");
            response.put("message", "Payment gateway error");
        }

        return response;
    }

    public Map<String, Object> refundPayment(String gatewayReference, BigDecimal amount) {
        log.info("Processing refund for gateway reference: {} amount: {}", gatewayReference, amount);

        Map<String, Object> response = new HashMap<>();
        
        try {
            // Simulate refund processing
            Thread.sleep(random.nextInt(300) + 200);

            // Simulate refund success (90% success rate for refunds)
            boolean isSuccessful = random.nextDouble() <= 0.9;

            if (isSuccessful) {
                response.put("success", true);
                response.put("refundReference", "REF_" + UUID.randomUUID().toString());
                response.put("message", "Refund processed successfully");
            } else {
                response.put("success", false);
                response.put("errorCode", "REFUND_FAILED");
                response.put("message", "Unable to process refund");
            }

            response.put("timestamp", System.currentTimeMillis());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.put("success", false);
            response.put("errorCode", "GATEWAY_ERROR");
            response.put("message", "Refund gateway error");
        }

        return response;
    }

    public Map<String, Object> checkPaymentStatus(String gatewayReference) {
        log.info("Checking payment status for gateway reference: {}", gatewayReference);

        Map<String, Object> response = new HashMap<>();
        response.put("gatewayReference", gatewayReference);
        response.put("status", "COMPLETED");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    private String generateAuthCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    private String generateCardLastFour() {
        return String.format("%04d", random.nextInt(9999));
    }

    private String detectCardBrand() {
        String[] brands = {"VISA", "MASTERCARD", "AMEX", "DISCOVER"};
        return brands[random.nextInt(brands.length)];
    }

    private String generateErrorCode() {
        String[] errorCodes = {
            "INSUFFICIENT_FUNDS",
            "CARD_DECLINED",
            "EXPIRED_CARD",
            "INVALID_CVV",
            "FRAUD_DETECTED",
            "LIMIT_EXCEEDED"
        };
        return errorCodes[random.nextInt(errorCodes.length)];
    }

    private String generateFailureReason() {
        Map<String, String> errorMessages = Map.of(
            "INSUFFICIENT_FUNDS", "Insufficient funds in account",
            "CARD_DECLINED", "Card declined by issuer",
            "EXPIRED_CARD", "Card has expired",
            "INVALID_CVV", "Invalid CVV code",
            "FRAUD_DETECTED", "Transaction flagged for fraud",
            "LIMIT_EXCEEDED", "Transaction limit exceeded"
        );
        
        String errorCode = generateErrorCode();
        return errorMessages.getOrDefault(errorCode, "Payment failed");
    }
}