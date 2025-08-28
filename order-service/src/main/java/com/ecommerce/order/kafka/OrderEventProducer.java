package com.ecommerce.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_UPDATED_TOPIC = "order-updated";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    private static final String INVENTORY_RESERVE_TOPIC = "inventory-reserve";
    private static final String PAYMENT_PROCESS_TOPIC = "payment-process";

    public void sendOrderCreatedEvent(Map<String, Object> orderEvent) {
        log.info("Sending order created event for order: {}", orderEvent.get("orderId"));
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(ORDER_CREATED_TOPIC, orderEvent.get("orderId").toString(), orderEvent);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order created event sent successfully for order: {}", orderEvent.get("orderId"));
            } else {
                log.error("Failed to send order created event for order: {}", orderEvent.get("orderId"), ex);
            }
        });
    }

    public void sendOrderUpdatedEvent(Map<String, Object> orderEvent) {
        log.info("Sending order updated event for order: {}", orderEvent.get("orderId"));
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(ORDER_UPDATED_TOPIC, orderEvent.get("orderId").toString(), orderEvent);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order updated event sent successfully for order: {}", orderEvent.get("orderId"));
            } else {
                log.error("Failed to send order updated event for order: {}", orderEvent.get("orderId"), ex);
            }
        });
    }

    public void sendOrderCancelledEvent(Map<String, Object> orderEvent) {
        log.info("Sending order cancelled event for order: {}", orderEvent.get("orderId"));
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, orderEvent.get("orderId").toString(), orderEvent);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order cancelled event sent successfully for order: {}", orderEvent.get("orderId"));
            } else {
                log.error("Failed to send order cancelled event for order: {}", orderEvent.get("orderId"), ex);
            }
        });
    }

    public void sendInventoryReserveRequest(Map<String, Object> reserveRequest) {
        log.info("Sending inventory reserve request for order: {}", reserveRequest.get("orderId"));
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(INVENTORY_RESERVE_TOPIC, reserveRequest.get("orderId").toString(), reserveRequest);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Inventory reserve request sent successfully for order: {}", reserveRequest.get("orderId"));
            } else {
                log.error("Failed to send inventory reserve request for order: {}", reserveRequest.get("orderId"), ex);
            }
        });
    }

    public void sendPaymentProcessRequest(Map<String, Object> paymentRequest) {
        log.info("Sending payment process request for order: {}", paymentRequest.get("orderId"));
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(PAYMENT_PROCESS_TOPIC, paymentRequest.get("orderId").toString(), paymentRequest);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Payment process request sent successfully for order: {}", paymentRequest.get("orderId"));
            } else {
                log.error("Failed to send payment process request for order: {}", paymentRequest.get("orderId"), ex);
            }
        });
    }
}