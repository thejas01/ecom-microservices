package com.ecommerce.notification.kafka;

import com.ecommerce.notification.entity.*;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "user-registered", groupId = "notification-service")
    public void handleUserRegistered(String message) {
        log.info("Received user-registered event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String userId = (String) event.get("userId");
            String email = (String) event.get("email");
            String firstName = (String) event.get("firstName");
            String lastName = (String) event.get("lastName");
            
            // Send welcome email
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("customerName", firstName + " " + lastName);
            templateVariables.put("email", email);
            templateVariables.put("userId", userId);
            
            Notification notification = Notification.builder()
                    .type(NotificationType.USER_REGISTRATION)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(email)
                    .subject("Welcome to Our E-Commerce Platform!")
                    .templateName("welcome-email")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(userId)
                    .referenceType("USER")
                    .build();
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error processing user-registered event", e);
        }
    }
    
    @KafkaListener(topics = "password-reset-requested", groupId = "notification-service")
    public void handlePasswordResetRequested(String message) {
        log.info("Received password-reset-requested event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String userId = (String) event.get("userId");
            String email = (String) event.get("email");
            String resetToken = (String) event.get("resetToken");
            String firstName = (String) event.get("firstName");
            
            // Send password reset email
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("customerName", firstName);
            templateVariables.put("resetLink", "https://ecommerce.com/reset-password?token=" + resetToken);
            templateVariables.put("expiryHours", "24");
            
            Notification notification = Notification.builder()
                    .type(NotificationType.PASSWORD_RESET)
                    .channel(NotificationChannel.EMAIL)
                    .recipient(email)
                    .subject("Password Reset Request")
                    .templateName("password-reset")
                    .content(objectMapper.writeValueAsString(templateVariables))
                    .referenceId(userId)
                    .referenceType("USER")
                    .build();
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            log.error("Error processing password-reset-requested event", e);
        }
    }
    
    @KafkaListener(topics = "user-profile-updated", groupId = "notification-service")
    public void handleUserProfileUpdated(String message) {
        log.info("Received user-profile-updated event: {}", message);
        
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String userId = (String) event.get("userId");
            String email = (String) event.get("email");
            String updateType = (String) event.get("updateType");
            
            // Send notification only for critical updates
            if ("email".equals(updateType) || "password".equals(updateType)) {
                String subject = "Security Update: Your Profile Has Been Modified";
                String content = String.format(
                        "Your %s has been updated. If you did not make this change, please contact support immediately.",
                        updateType
                );
                
                Notification notification = Notification.builder()
                        .type(NotificationType.PROMOTIONAL) // Using PROMOTIONAL as a general type
                        .channel(NotificationChannel.EMAIL)
                        .recipient(email)
                        .subject(subject)
                        .content(content)
                        .referenceId(userId)
                        .referenceType("USER")
                        .build();
                
                notificationService.createNotification(notification);
            }
            
        } catch (Exception e) {
            log.error("Error processing user-profile-updated event", e);
        }
    }
}