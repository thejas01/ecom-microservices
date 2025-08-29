package com.ecommerce.payment.kafka;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "payment-service")
    public void handleOrderCreated(String eventJson) {
        try {
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
            log.info("Received order created event: {}", event.get("orderId"));

            String orderId = (String) event.get("orderId");
            String customerId = (String) event.get("customerId");
            BigDecimal amount = new BigDecimal(event.get("totalAmount").toString());

            // Check if payment already exists for this order
            if (paymentService.getPaymentByOrderId(orderId).isPresent()) {
                log.warn("Payment already exists for order: {}", orderId);
                return;
            }

            // Create payment for the order
            Payment payment = Payment.builder()
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .currency("USD")
                .paymentMethod("card")
                .build();

            Payment createdPayment = paymentService.createPayment(payment);
            log.info("Payment created for order: {} with payment ID: {}", orderId, createdPayment.getId());

            // Auto-process payment if enabled
            if (shouldAutoProcessPayment(event)) {
                try {
                    paymentService.processPayment(createdPayment.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-process payment: {}", createdPayment.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error processing order created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "payment-service")
    public void handleOrderCancelled(String eventJson) {
        try {
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
            log.info("Received order cancelled event: {}", event.get("orderId"));

            String orderId = (String) event.get("orderId");

            paymentService.getPaymentByOrderId(orderId).ifPresent(payment -> {
                switch (payment.getStatus()) {
                    case PENDING:
                    case PROCESSING:
                        try {
                            paymentService.cancelPayment(payment.getId());
                            log.info("Payment cancelled for order: {}", orderId);
                        } catch (Exception e) {
                            log.error("Failed to cancel payment: {}", payment.getId(), e);
                        }
                        break;
                    case COMPLETED:
                        // Initiate refund for completed payments
                        try {
                            paymentService.refundPayment(payment.getId(), payment.getAmount());
                            log.info("Refund initiated for cancelled order: {}", orderId);
                        } catch (Exception e) {
                            log.error("Failed to refund payment: {}", payment.getId(), e);
                        }
                        break;
                    default:
                        log.info("No action needed for payment status: {} for order: {}", 
                            payment.getStatus(), orderId);
                }
            });

        } catch (Exception e) {
            log.error("Error processing order cancelled event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-completed", groupId = "payment-service")
    public void handleOrderCompleted(String eventJson) {
        try {
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
            log.info("Received order completed event: {}", event.get("orderId"));

            String orderId = (String) event.get("orderId");

            // Verify payment was successful
            if (!paymentService.isPaymentSuccessful(orderId)) {
                log.error("Order completed but payment not successful for order: {}", orderId);
            }

        } catch (Exception e) {
            log.error("Error processing order completed event: {}", e.getMessage(), e);
        }
    }

    private boolean shouldAutoProcessPayment(Map<String, Object> event) {
        // Check if auto-process flag is set in the event
        Object autoProcess = event.get("autoProcessPayment");
        if (autoProcess != null) {
            return Boolean.parseBoolean(autoProcess.toString());
        }
        
        // Default behavior - don't auto-process
        return false;
    }
}