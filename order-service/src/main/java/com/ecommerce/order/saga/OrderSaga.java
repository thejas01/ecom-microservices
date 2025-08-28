package com.ecommerce.order.saga;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class OrderSaga {
    
    private String orderId;
    private SagaStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // Saga steps tracking
    private boolean orderCreated;
    private boolean inventoryReserved;
    private boolean paymentCompleted;
    private boolean orderConfirmed;
    
    // Rollback tracking
    private boolean inventoryReleased;
    private boolean paymentRefunded;
    private boolean orderCancelled;
    
    // Error tracking
    private String failureReason;
    private String failedStep;
    
    // Audit trail
    private List<SagaEvent> events;
    
    public OrderSaga(String orderId) {
        this.orderId = orderId;
        this.status = SagaStatus.STARTED;
        this.startTime = LocalDateTime.now();
        this.events = new ArrayList<>();
        
        addEvent("SAGA_STARTED", "Order saga initiated");
    }
    
    public void markOrderCreated() {
        this.orderCreated = true;
        addEvent("ORDER_CREATED", "Order created successfully");
    }
    
    public void markInventoryReserved() {
        this.inventoryReserved = true;
        addEvent("INVENTORY_RESERVED", "Inventory reserved successfully");
    }
    
    public void markPaymentCompleted() {
        this.paymentCompleted = true;
        addEvent("PAYMENT_COMPLETED", "Payment processed successfully");
    }
    
    public void markOrderConfirmed() {
        this.orderConfirmed = true;
        this.status = SagaStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        addEvent("ORDER_CONFIRMED", "Order confirmed and saga completed");
    }
    
    public void markInventoryReleased() {
        this.inventoryReleased = true;
        addEvent("INVENTORY_RELEASED", "Inventory released due to rollback");
    }
    
    public void markPaymentRefunded() {
        this.paymentRefunded = true;
        addEvent("PAYMENT_REFUNDED", "Payment refunded due to rollback");
    }
    
    public void markOrderCancelled() {
        this.orderCancelled = true;
        this.status = SagaStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        addEvent("ORDER_CANCELLED", "Order cancelled and saga rolled back");
    }
    
    public void markFailed(String step, String reason) {
        this.failedStep = step;
        this.failureReason = reason;
        this.status = SagaStatus.FAILED;
        this.endTime = LocalDateTime.now();
        addEvent("SAGA_FAILED", String.format("Saga failed at step: %s, reason: %s", step, reason));
    }
    
    public boolean requiresRollback() {
        return status == SagaStatus.FAILED || status == SagaStatus.CANCELLING;
    }
    
    public void startRollback() {
        this.status = SagaStatus.CANCELLING;
        addEvent("ROLLBACK_STARTED", "Starting saga rollback");
    }
    
    private void addEvent(String eventType, String description) {
        SagaEvent event = new SagaEvent(eventType, description, LocalDateTime.now());
        events.add(event);
        log.info("Saga Event - Order: {}, Type: {}, Description: {}", orderId, eventType, description);
    }
    
    public enum SagaStatus {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLING,
        CANCELLED
    }
    
    @Data
    public static class SagaEvent {
        private final String eventType;
        private final String description;
        private final LocalDateTime timestamp;
        
        public SagaEvent(String eventType, String description, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.description = description;
            this.timestamp = timestamp;
        }
    }
}