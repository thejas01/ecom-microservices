package com.ecommerce.notification.kafka;

import com.ecommerce.notification.entity.*;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order-created", groupId = "notification-service")
    public void handleOrderCreated(String message) {
        log.info("Received order-created event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String orderId = (String) event.get("orderId");
            String customerId = (String) event.get("customerId");
            String customerEmail = (String) event.get("customerEmail");
            String orderNumber = (String) event.get("orderNumber");
            BigDecimal totalAmount = new BigDecimal(event.get("totalAmount").toString());
            List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");
            
            // Prepare template variables
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("orderNumber", orderNumber);
            templateVariables.put("orderId", orderId);
            templateVariables.put("totalAmount", totalAmount);
            templateVariables.put("itemCount", items.size());
            templateVariables.put("items", items);
            templateVariables.put("customerName", event.get("customerName"));
            
            // Send order confirmation email
            Notification notification = Notification.builder()
                    .type(NotificationType.ORDER_CONFIRMATION)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(customerEmail)
                    .subject("Order Confirmation - Order #" + orderNumber)
                    .templateName("order-confirmation")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .build();
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error processing order-created event", e);
        }
    }
    
    @KafkaListener(topics = "order-status-updated", groupId = "notification-service")
    public void handleOrderStatusUpdated(String message) {
        log.info("Received order-status-updated event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String orderId = (String) event.get("orderId");
            String orderNumber = (String) event.get("orderNumber");
            String customerEmail = (String) event.get("customerEmail");
            String oldStatus = (String) event.get("oldStatus");
            String newStatus = (String) event.get("newStatus");
            
            // Only send notifications for specific status changes
            if (shouldNotifyStatusChange(oldStatus, newStatus)) {
                NotificationType notificationType = getNotificationTypeForStatus(newStatus);
                String subject = getSubjectForStatus(newStatus, orderNumber);
                String templateName = getTemplateForStatus(newStatus);
                
                Map<String, Object> templateVariables = new HashMap<>();
                templateVariables.put("orderNumber", orderNumber);
                templateVariables.put("orderId", orderId);
                templateVariables.put("status", newStatus);
                templateVariables.put("customerName", event.get("customerName"));
                
                // Add tracking info for shipped orders
                if ("SHIPPED".equals(newStatus) && event.containsKey("trackingNumber")) {
                    templateVariables.put("trackingNumber", event.get("trackingNumber"));
                    templateVariables.put("carrier", event.get("carrier"));
                }
                
                Notification notification = Notification.builder()
                        .type(notificationType)
                        .channel(NotificationChannel.EMAIL)
                        .recipient(customerEmail)
                        .subject(subject)
                        .templateName(templateName)
                        .content(objectMapper.writeValueAsString(templateVariables))
                        .referenceId(orderId)
                        .referenceType("ORDER")
                        .build();
                
                notificationService.createNotification(notification);
            }
            
        } catch (Exception e) {
            log.error("Error processing order-status-updated event", e);
        }
    }
    
    @KafkaListener(topics = "order-cancelled", groupId = "notification-service")
    public void handleOrderCancelled(String message) {
        log.info("Received order-cancelled event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String orderId = (String) event.get("orderId");
            String orderNumber = (String) event.get("orderNumber");
            String customerEmail = (String) event.get("customerEmail");
            String cancellationReason = (String) event.get("cancellationReason");
            BigDecimal refundAmount = event.containsKey("refundAmount") 
                    ? new BigDecimal(event.get("refundAmount").toString()) 
                    : null;
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("orderNumber", orderNumber);
            templateVariables.put("orderId", orderId);
            templateVariables.put("cancellationReason", cancellationReason);
            templateVariables.put("customerName", event.get("customerName"));
            
            if (refundAmount != null) {
                templateVariables.put("refundAmount", refundAmount);
                templateVariables.put("refundMessage", "A refund of $" + refundAmount + " will be processed within 3-5 business days.");
            }
            
            Notification notification = Notification.builder()
                    .type(NotificationType.ORDER_CANCELLED)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(customerEmail)
                    .subject("Order Cancelled - Order #" + orderNumber)
                    .templateName("order-cancelled")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .build();
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error processing order-cancelled event", e);
        }
    }
    
    private boolean shouldNotifyStatusChange(String oldStatus, String newStatus) {
        // Define which status transitions should trigger notifications
        return ("SHIPPED".equals(newStatus) && !"SHIPPED".equals(oldStatus)) ||
               ("DELIVERED".equals(newStatus) && !"DELIVERED".equals(oldStatus)) ||
               ("OUT_FOR_DELIVERY".equals(newStatus) && !"OUT_FOR_DELIVERY".equals(oldStatus));
    }
    
    private com.ecommerce.notification.entity.NotificationType getNotificationTypeForStatus(String status) {
        switch (status) {
            case "SHIPPED":
                return NotificationType.ORDER_SHIPPED;
            case "DELIVERED":
                return NotificationType.ORDER_DELIVERED;
            case "CANCELLED":
                return NotificationType.ORDER_CANCELLED;
            default:
                return NotificationType.ORDER_CONFIRMATION;
        }
    }
    
    private String getSubjectForStatus(String status, String orderNumber) {
        switch (status) {
            case "SHIPPED":
                return "Your Order Has Been Shipped - Order #" + orderNumber;
            case "DELIVERED":
                return "Your Order Has Been Delivered - Order #" + orderNumber;
            case "OUT_FOR_DELIVERY":
                return "Your Order is Out for Delivery - Order #" + orderNumber;
            default:
                return "Order Update - Order #" + orderNumber;
        }
    }
    
    private String getTemplateForStatus(String status) {
        switch (status) {
            case "SHIPPED":
                return "order-shipped";
            case "DELIVERED":
                return "order-delivered";
            case "CANCELLED":
                return "order-cancelled";
            default:
                return "order-confirmation";
        }
    }
}