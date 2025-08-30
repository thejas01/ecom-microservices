package com.ecommerce.notification.kafka;

import com.ecommerce.notification.entity.*;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    @KafkaListener(topics = "payment-completed", groupId = "notification-service")
    public void handlePaymentCompleted(String message) {
        log.info("Received payment-completed event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String paymentId = (String) event.get("paymentId");
            String orderId = (String) event.get("orderId");
            String customerId = (String) event.get("customerId");
            String customerEmail = (String) event.get("customerEmail");
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String currency = (String) event.get("currency");
            String paymentMethod = (String) event.get("paymentMethod");
            String transactionId = (String) event.get("transactionId");
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("paymentId", paymentId);
            templateVariables.put("orderId", orderId);
            templateVariables.put("amount", amount);
            templateVariables.put("currency", currency);
            templateVariables.put("paymentMethod", formatPaymentMethod(paymentMethod));
            templateVariables.put("transactionId", transactionId);
            templateVariables.put("paymentDate", LocalDateTime.now().format(DATETIME_FORMATTER));
            templateVariables.put("customerName", event.get("customerName"));
            
            // Send payment success email
            Notification emailNotification = Notification.builder()
                    .type(NotificationType.PAYMENT_SUCCESS)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(customerEmail)
                    .subject("Payment Successful - Transaction #" + transactionId)
                    .templateName("payment-success")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(paymentId)
                    .referenceType("PAYMENT")
                    .build();
            
            notificationService.createNotification(emailNotification);
            
            // Also send SMS if phone number is provided
            if (event.containsKey("customerPhone") && event.get("customerPhone") != null) {
                String phoneNumber = (String) event.get("customerPhone");
                
                String smsMessage = String.format(
                        "Payment of %s %s for Order #%s has been processed successfully. Transaction ID: %s",
                        currency, amount, orderId, transactionId
                );
                
                Notification smsNotification = Notification.builder()
                        .type(NotificationType.PAYMENT_SUCCESS)
                        .channel(NotificationChannel.SMS)
                        .recipient(phoneNumber)
                        .content(smsMessage)
                        .referenceId(paymentId)
                        .referenceType("PAYMENT")
                        .build();
                
                notificationService.createNotification(smsNotification);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment-completed event", e);
        }
    }
    
    @KafkaListener(topics = "payment-failed", groupId = "notification-service")
    public void handlePaymentFailed(String message) {
        log.info("Received payment-failed event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String paymentId = (String) event.get("paymentId");
            String orderId = (String) event.get("orderId");
            String customerEmail = (String) event.get("customerEmail");
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String currency = (String) event.get("currency");
            String failureReason = (String) event.get("failureReason");
            String paymentMethod = (String) event.get("paymentMethod");
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("paymentId", paymentId);
            templateVariables.put("orderId", orderId);
            templateVariables.put("amount", amount);
            templateVariables.put("currency", currency);
            templateVariables.put("failureReason", failureReason);
            templateVariables.put("paymentMethod", formatPaymentMethod(paymentMethod));
            templateVariables.put("customerName", event.get("customerName"));
            templateVariables.put("retryMessage", "Please try again or use a different payment method.");
            
            Notification notification = Notification.builder()
                    .type(NotificationType.PAYMENT_FAILED)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(customerEmail)
                    .subject("Payment Failed - Order #" + orderId)
                    .templateName("payment-failed")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(paymentId)
                    .referenceType("PAYMENT")
                    .build();
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error processing payment-failed event", e);
        }
    }
    
    @KafkaListener(topics = "payment-refunded", groupId = "notification-service")
    public void handlePaymentRefunded(String message) {
        log.info("Received payment-refunded event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String paymentId = (String) event.get("paymentId");
            String orderId = (String) event.get("orderId");
            String customerEmail = (String) event.get("customerEmail");
            BigDecimal originalAmount = new BigDecimal(event.get("originalAmount").toString());
            BigDecimal refundAmount = new BigDecimal(event.get("refundAmount").toString());
            String currency = (String) event.get("currency");
            String refundReason = (String) event.get("refundReason");
            String refundId = (String) event.get("refundId");
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("paymentId", paymentId);
            templateVariables.put("orderId", orderId);
            templateVariables.put("originalAmount", originalAmount);
            templateVariables.put("refundAmount", refundAmount);
            templateVariables.put("currency", currency);
            templateVariables.put("refundReason", refundReason);
            templateVariables.put("refundId", refundId);
            templateVariables.put("refundDate", LocalDateTime.now().format(DATETIME_FORMATTER));
            templateVariables.put("customerName", event.get("customerName"));
            templateVariables.put("processingTime", "3-5 business days");
            
            // Determine if it's a full or partial refund
            boolean isFullRefund = originalAmount.compareTo(refundAmount) == 0;
            templateVariables.put("isFullRefund", isFullRefund);
            templateVariables.put("refundType", isFullRefund ? "Full Refund" : "Partial Refund");
            
            Notification emailNotification = Notification.builder()
                    .type(NotificationType.PAYMENT_REFUNDED)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(customerEmail)
                    .subject("Refund Processed - Order #" + orderId)
                    .templateName("payment-refunded")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(paymentId)
                    .referenceType("PAYMENT")
                    .build();
            
            notificationService.createNotification(emailNotification);
            
            // Send SMS notification for refunds
            if (event.containsKey("customerPhone") && event.get("customerPhone") != null) {
                String phoneNumber = (String) event.get("customerPhone");
                
                String smsMessage = String.format(
                        "Refund of %s %s for Order #%s has been processed. Refund ID: %s. Expect funds in 3-5 business days.",
                        currency, refundAmount, orderId, refundId
                );
                
                Notification smsNotification = Notification.builder()
                        .type(NotificationType.PAYMENT_REFUNDED)
                        .channel(NotificationChannel.SMS)
                        .recipient(phoneNumber)
                        .content(smsMessage)
                        .referenceId(paymentId)
                        .referenceType("PAYMENT")
                        .build();
                
                notificationService.createNotification(smsNotification);
            }
            
        } catch (Exception e) {
            log.error("Error processing payment-refunded event", e);
        }
    }
    
    @KafkaListener(topics = "payment-reminder", groupId = "notification-service")
    public void handlePaymentReminder(String message) {
        log.info("Received payment-reminder event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String orderId = (String) event.get("orderId");
            String customerEmail = (String) event.get("customerEmail");
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String currency = (String) event.get("currency");
            Integer hoursRemaining = (Integer) event.get("hoursRemaining");
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("orderId", orderId);
            templateVariables.put("amount", amount);
            templateVariables.put("currency", currency);
            templateVariables.put("hoursRemaining", hoursRemaining);
            templateVariables.put("customerName", event.get("customerName"));
            templateVariables.put("paymentLink", event.get("paymentLink"));
            
            String urgency = hoursRemaining <= 2 ? "urgent" : "reminder";
            templateVariables.put("urgencyLevel", urgency);
            
            Notification notification = Notification.builder()
                    .type(NotificationType.PAYMENT_FAILED) // Using as reminder type
                    .channel(NotificationChannel.EMAIL)
                    .recipient(customerEmail)
                    .subject("Payment Reminder - Order #" + orderId + " expires in " + hoursRemaining + " hours")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .build();
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error processing payment-reminder event", e);
        }
    }
    
    private String formatPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return "Unknown";
        }
        
        switch (paymentMethod.toUpperCase()) {
            case "CREDIT_CARD":
                return "Credit Card";
            case "DEBIT_CARD":
                return "Debit Card";
            case "PAYPAL":
                return "PayPal";
            case "BANK_TRANSFER":
                return "Bank Transfer";
            case "WALLET":
                return "Digital Wallet";
            default:
                return paymentMethod;
        }
    }
}