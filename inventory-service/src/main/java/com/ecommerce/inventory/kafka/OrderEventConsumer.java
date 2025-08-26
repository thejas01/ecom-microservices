package com.ecommerce.inventory.kafka;

import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    public void handleOrderCreated(String message) {
        log.info("Received order created event: {}", message);
        
        try {
            Map<String, Object> orderEvent = objectMapper.readValue(message, Map.class);
            String orderId = (String) orderEvent.get("orderId");
            String productId = (String) orderEvent.get("productId");
            Integer quantity = objectMapper.convertValue(orderEvent.get("quantity"), Integer.class);

            log.info("Processing order: {} for product: {} quantity: {}", orderId, productId, quantity);

            boolean reserved = inventoryService.reserveStock(productId, quantity);
            
            if (reserved) {
                log.info("Stock reserved successfully for order: {}", orderId);
                inventoryEventProducer.publishStockReserved(productId, quantity, orderId);
            } else {
                log.warn("Insufficient stock for order: {}", orderId);
                // In a real system, you might publish an order failed event here
            }
            
        } catch (Exception e) {
            log.error("Error processing order created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-confirmed", groupId = "inventory-service-group")
    public void handleOrderConfirmed(String message) {
        log.info("Received order confirmed event: {}", message);
        
        try {
            Map<String, Object> orderEvent = objectMapper.readValue(message, Map.class);
            String orderId = (String) orderEvent.get("orderId");
            String productId = (String) orderEvent.get("productId");
            Integer quantity = objectMapper.convertValue(orderEvent.get("quantity"), Integer.class);

            log.info("Confirming order: {} for product: {} quantity: {}", orderId, productId, quantity);

            inventoryService.confirmStockReduction(productId, quantity);
            inventoryEventProducer.publishInventoryUpdated(productId, 
                inventoryService.getInventoryByProductId(productId)
                    .map(inv -> inv.getAvailableQuantity())
                    .orElse(0));
            
        } catch (Exception e) {
            log.error("Error processing order confirmed event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "inventory-service-group")
    public void handleOrderCancelled(String message) {
        log.info("Received order cancelled event: {}", message);
        
        try {
            Map<String, Object> orderEvent = objectMapper.readValue(message, Map.class);
            String orderId = (String) orderEvent.get("orderId");
            String productId = (String) orderEvent.get("productId");
            Integer quantity = objectMapper.convertValue(orderEvent.get("quantity"), Integer.class);

            log.info("Cancelling order: {} for product: {} quantity: {}", orderId, productId, quantity);

            inventoryService.releaseReservedStock(productId, quantity);
            inventoryEventProducer.publishStockReleased(productId, quantity, orderId);
            
        } catch (Exception e) {
            log.error("Error processing order cancelled event: {}", e.getMessage(), e);
        }
    }
}