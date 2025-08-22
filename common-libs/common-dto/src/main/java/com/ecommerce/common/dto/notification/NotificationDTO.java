package com.ecommerce.common.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {
    
    private String id;
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String templateName;
    
    private Map<String, Object> templateData;
    
    private NotificationStatus status;
    
    private String recipient; // email address or phone number
    
    private Integer retryCount;
    
    private String errorMessage;
    
    private LocalDateTime scheduledAt;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime readAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum NotificationType {
        ORDER_CONFIRMATION,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        WELCOME,
        PASSWORD_RESET,
        PROMOTIONAL,
        ACCOUNT_UPDATE
    }
    
    public enum NotificationChannel {
        EMAIL, SMS, PUSH, IN_APP
    }
    
    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, FAILED, READ, CANCELLED
    }
}