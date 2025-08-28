package com.ecommerce.order.service;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.kafka.OrderEventProducer;
import com.ecommerce.order.saga.OrderSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {

    private final OrderService orderService;
    private final OrderEventProducer orderEventProducer;
    private final RestTemplate restTemplate;
    private static final String INVENTORY_SERVICE_URL = "http://INVENTORY-SERVICE/inventory";
    private static final String PAYMENT_SERVICE_URL = "http://PAYMENT-SERVICE/payments";

    @Transactional
    public Order processOrder(Order order, String authToken) {
        log.info("Starting order orchestration for customer: {}", order.getCustomerId());
        OrderSaga saga = new OrderSaga(order.getId());
        
        try {
            // Step 1: Create order in PENDING state
            Order createdOrder = orderService.createOrder(order);
            saga.markOrderCreated();
            
            // Step 2: Reserve inventory for all items
            boolean inventoryReserved = reserveInventory(createdOrder, authToken);
            if (!inventoryReserved) {
                log.error("Failed to reserve inventory for order: {}", createdOrder.getId());
                orderService.updateOrderStatus(createdOrder.getId(), OrderStatus.INVENTORY_FAILED);
                throw new RuntimeException("Insufficient inventory");
            }
            saga.markInventoryReserved();
            createdOrder.setStatus(OrderStatus.INVENTORY_RESERVED);
            
            // Step 3: Process payment
            boolean paymentProcessed = processPayment(createdOrder, authToken);
            if (!paymentProcessed) {
                log.error("Payment failed for order: {}", createdOrder.getId());
                releaseInventory(createdOrder, authToken);
                orderService.updateOrderStatus(createdOrder.getId(), OrderStatus.PAYMENT_FAILED);
                throw new RuntimeException("Payment processing failed");
            }
            saga.markPaymentCompleted();
            
            // Step 4: Confirm order
            createdOrder = orderService.updateOrderStatus(createdOrder.getId(), OrderStatus.PROCESSING);
            
            // Step 5: Publish order confirmed event
            publishOrderConfirmedEvent(createdOrder);
            
            log.info("Order {} processed successfully", createdOrder.getId());
            return createdOrder;
            
        } catch (Exception e) {
            log.error("Order processing failed: {}", e.getMessage());
            handleSagaRollback(saga, order, authToken);
            throw e;
        }
    }

    private boolean reserveInventory(Order order, String authToken) {
        log.info("Reserving inventory for order: {}", order.getId());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            
            for (var item : order.getItems()) {
                Map<String, Object> reserveRequest = new HashMap<>();
                reserveRequest.put("productId", item.getProductId());
                reserveRequest.put("quantity", item.getQuantity());
                reserveRequest.put("orderId", order.getId());
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(reserveRequest, headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                    INVENTORY_SERVICE_URL + "/reserve",
                    HttpMethod.POST,
                    request,
                    Map.class
                );
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("Failed to reserve inventory for product: {}", item.getProductId());
                    return false;
                }
                
                // Check if reservation was successful
                Map<String, Object> responseBody = response.getBody();
                if (responseBody == null || !(Boolean) responseBody.get("success")) {
                    log.error("Inventory reservation failed for product: {} - {}", item.getProductId(), 
                             responseBody != null ? responseBody.get("message") : "Unknown error");
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error reserving inventory: {}", e.getMessage());
            return false;
        }
    }

    private void releaseInventory(Order order, String authToken) {
        log.info("Releasing inventory for order: {}", order.getId());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            
            for (var item : order.getItems()) {
                Map<String, Object> releaseRequest = new HashMap<>();
                releaseRequest.put("productId", item.getProductId());
                releaseRequest.put("quantity", item.getQuantity());
                releaseRequest.put("orderId", order.getId());
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(releaseRequest, headers);
                restTemplate.exchange(
                    INVENTORY_SERVICE_URL + "/release",
                    HttpMethod.POST,
                    request,
                    String.class
                );
            }
        } catch (Exception e) {
            log.error("Error releasing inventory: {}", e.getMessage());
        }
    }

    private boolean processPayment(Order order, String authToken) {
        log.info("Processing payment for order: {} - SKIPPING (Payment service not implemented)", order.getId());
        
        // TODO: Implement actual payment processing when payment service is available
        // For now, we simulate successful payment processing
        try {
            // Simulate payment processing delay
            Thread.sleep(100);
            
            // Mock payment details
            String mockPaymentId = "PAY_" + order.getId().toString().substring(0, 8);
            orderService.updatePaymentInfo(order.getId(), mockPaymentId, "COMPLETED");
            
            log.info("Mock payment processed successfully for order: {} with payment ID: {}", order.getId(), mockPaymentId);
            return true;
            
        } catch (Exception e) {
            log.error("Error in mock payment processing: {}", e.getMessage());
            return false;
        }
    }

    private void publishOrderConfirmedEvent(Order order) {
        log.info("Publishing order confirmed event for order: {}", order.getId());
        
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId());
        event.put("orderNumber", order.getOrderNumber());
        event.put("customerId", order.getCustomerId());
        event.put("totalAmount", order.getTotalAmount());
        event.put("status", order.getStatus().toString());
        event.put("items", order.getItems());
        
        orderEventProducer.sendOrderCreatedEvent(event);
    }

    private void handleSagaRollback(OrderSaga saga, Order order, String authToken) {
        log.info("Handling saga rollback for order: {}", order.getId());
        
        if (saga.isInventoryReserved() && !saga.isPaymentCompleted()) {
            releaseInventory(order, authToken);
        }
        
        if (saga.isOrderCreated()) {
            try {
                orderService.cancelOrder(order.getId(), "Order processing failed - automatic cancellation");
            } catch (Exception e) {
                log.error("Error cancelling order during rollback: {}", e.getMessage());
            }
        }
    }
}