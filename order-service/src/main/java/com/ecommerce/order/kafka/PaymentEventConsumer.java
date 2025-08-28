package com.ecommerce.order.kafka;

import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-completed", groupId = "order-service")
    public void handlePaymentCompleted(String message) {
        log.info("Received payment completed event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String paymentId = (String) event.get("paymentId");
            String status = (String) event.get("status");
            
            log.info("Processing payment completion for order: {}, payment: {}, status: {}", orderId, paymentId, status);
            
            orderService.updatePaymentInfo(orderId, paymentId, status);
            
            if ("COMPLETED".equalsIgnoreCase(status)) {
                orderService.updateOrderStatus(orderId, OrderStatus.PAYMENT_COMPLETED);
                log.info("Order {} marked as payment completed", orderId);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment completed event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "order-service")
    public void handlePaymentFailed(String message) {
        log.info("Received payment failed event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String paymentId = (String) event.get("paymentId");
            String reason = (String) event.get("reason");
            
            log.info("Processing payment failure for order: {}, reason: {}", orderId, reason);
            
            orderService.updatePaymentInfo(orderId, paymentId, "FAILED");
            orderService.updateOrderStatus(orderId, OrderStatus.PAYMENT_FAILED);
            
            // TODO: Trigger inventory release for failed payment
            
        } catch (Exception e) {
            log.error("Error processing payment failed event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "inventory-reserved", groupId = "order-service")
    public void handleInventoryReserved(String message) {
        log.info("Received inventory reserved event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            
            log.info("Processing inventory reservation confirmation for order: {}", orderId);
            orderService.updateOrderStatus(orderId, OrderStatus.INVENTORY_RESERVED);
            
        } catch (Exception e) {
            log.error("Error processing inventory reserved event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "inventory-failed", groupId = "order-service")
    public void handleInventoryFailed(String message) {
        log.info("Received inventory failed event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String reason = (String) event.get("reason");
            
            log.info("Processing inventory failure for order: {}, reason: {}", orderId, reason);
            orderService.updateOrderStatus(orderId, OrderStatus.INVENTORY_FAILED);
            
        } catch (Exception e) {
            log.error("Error processing inventory failed event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "shipment-created", groupId = "order-service")
    public void handleShipmentCreated(String message) {
        log.info("Received shipment created event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String trackingNumber = (String) event.get("trackingNumber");
            
            log.info("Processing shipment creation for order: {}, tracking: {}", orderId, trackingNumber);
            
            orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);
            // TODO: Update tracking number in order
            
        } catch (Exception e) {
            log.error("Error processing shipment created event: {}", e.getMessage(), e);
        }
    }
}