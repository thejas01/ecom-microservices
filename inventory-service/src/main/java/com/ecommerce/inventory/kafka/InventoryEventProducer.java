package com.ecommerce.inventory.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String INVENTORY_UPDATED_TOPIC = "inventory-updated";
    private static final String INVENTORY_LOW_STOCK_TOPIC = "inventory-low-stock";

    public void publishInventoryUpdated(String productId, Integer availableQuantity) {
        log.info("Publishing inventory updated event for product: {}", productId);
        
        Map<String, Object> event = Map.of(
                "eventType", "INVENTORY_UPDATED",
                "productId", productId,
                "availableQuantity", availableQuantity,
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(INVENTORY_UPDATED_TOPIC, productId, event);
    }

    public void publishLowStockAlert(String productId, Integer currentQuantity, Integer threshold) {
        log.warn("Publishing low stock alert for product: {}", productId);
        
        Map<String, Object> event = Map.of(
                "eventType", "LOW_STOCK_ALERT",
                "productId", productId,
                "currentQuantity", currentQuantity,
                "threshold", threshold,
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(INVENTORY_LOW_STOCK_TOPIC, productId, event);
    }

    public void publishStockReserved(String productId, Integer reservedQuantity, String orderId) {
        log.info("Publishing stock reserved event for product: {} order: {}", productId, orderId);
        
        Map<String, Object> event = Map.of(
                "eventType", "STOCK_RESERVED",
                "productId", productId,
                "reservedQuantity", reservedQuantity,
                "orderId", orderId,
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(INVENTORY_UPDATED_TOPIC, productId, event);
    }

    public void publishStockReleased(String productId, Integer releasedQuantity, String orderId) {
        log.info("Publishing stock released event for product: {} order: {}", productId, orderId);
        
        Map<String, Object> event = Map.of(
                "eventType", "STOCK_RELEASED",
                "productId", productId,
                "releasedQuantity", releasedQuantity,
                "orderId", orderId,
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(INVENTORY_UPDATED_TOPIC, productId, event);
    }
}