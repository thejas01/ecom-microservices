package com.ecommerce.payment.kafka;

import com.ecommerce.payment.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentCreatedEvent(Payment payment) {
        sendEvent("payment-created", payment, "Payment created");
    }

    public void sendPaymentCompletedEvent(Payment payment) {
        sendEvent("payment-completed", payment, "Payment completed successfully");
    }

    public void sendPaymentFailedEvent(Payment payment) {
        Map<String, Object> event = createEventMap(payment, "Payment failed");
        event.put("failureReason", payment.getFailureReason());
        sendEvent("payment-failed", event);
    }

    public void sendPaymentCancelledEvent(Payment payment) {
        sendEvent("payment-cancelled", payment, "Payment cancelled");
    }

    public void sendPaymentRefundedEvent(Payment payment, BigDecimal refundAmount) {
        Map<String, Object> event = createEventMap(payment, "Payment refunded");
        event.put("refundAmount", refundAmount);
        event.put("totalRefunded", payment.getRefundAmount());
        sendEvent("payment-refunded", event);
    }

    public void sendPaymentExpiredEvent(Payment payment) {
        sendEvent("payment-expired", payment, "Payment expired");
    }

    private void sendEvent(String topic, Payment payment, String message) {
        Map<String, Object> event = createEventMap(payment, message);
        sendEvent(topic, event);
    }

    private Map<String, Object> createEventMap(Payment payment, String message) {
        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", payment.getId());
        event.put("orderId", payment.getOrderId());
        event.put("customerId", payment.getCustomerId());
        event.put("amount", payment.getAmount());
        event.put("currency", payment.getCurrency());
        event.put("status", payment.getStatus());
        event.put("transactionId", payment.getTransactionId());
        event.put("timestamp", System.currentTimeMillis());
        event.put("message", message);
        return event;
    }

    private void sendEvent(String topic, Map<String, Object> event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, event.get("orderId").toString(), eventJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event sent to topic [{}]: {}", topic, event.get("orderId"));
                } else {
                    log.error("Failed to send event to topic [{}]: {}", topic, event.get("orderId"), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending event to topic [{}]: {}", topic, e.getMessage(), e);
        }
    }
}