package com.ecommerce.notification.controller;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody Notification notification) {
        log.info("Creating notification - Type: {}, Channel: {}, Recipient: {}", 
                notification.getType(), notification.getChannel(), notification.getRecipient());
        
        Notification createdNotification = notificationService.createNotification(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
    }
    
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<Map<String, Object>> createBatchNotifications(
            @Valid @RequestBody List<Notification> notifications) {
        log.info("Creating batch notifications - Count: {}", notifications.size());
        
        Map<String, Object> response = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Notification notification : notifications) {
            try {
                notificationService.createNotification(notification);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create notification for recipient: {}", notification.getRecipient(), e);
                failureCount++;
            }
        }
        
        response.put("total", notifications.size());
        response.put("success", successCount);
        response.put("failed", failureCount);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('ADMIN') or @notificationService.getNotification(#notificationId).recipient == authentication.name")
    public ResponseEntity<Notification> getNotification(@PathVariable UUID notificationId) {
        log.info("Fetching notification: {}", notificationId);
        
        Notification notification = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(notification);
    }
    
    @GetMapping("/recipient/{recipient}")
    @PreAuthorize("hasAnyRole('ADMIN') or #recipient == authentication.name")
    public ResponseEntity<Page<Notification>> getNotificationsByRecipient(
            @PathVariable String recipient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("Fetching notifications for recipient: {}", recipient);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Notification> notifications = notificationService.getNotificationsByRecipient(recipient, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/my-notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {
        
        String recipient = authentication.getName();
        log.info("Fetching notifications for authenticated user: {}", recipient);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Notification> notifications = notificationService.getNotificationsByRecipient(recipient, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/reference/{referenceType}/{referenceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Notification>> getNotificationsByReference(
            @PathVariable String referenceType,
            @PathVariable String referenceId) {
        
        log.info("Fetching notifications for reference: {} - {}", referenceType, referenceId);
        
        List<Notification> notifications = notificationService.getNotificationsByReference(referenceId, referenceType);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread-count/{recipient}")
    @PreAuthorize("hasAnyRole('ADMIN') or #recipient == authentication.name")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String recipient) {
        log.info("Getting unread count for recipient: {}", recipient);
        
        Long unreadCount = notificationService.getUnreadCount(recipient);
        
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/{recipient}")
    @PreAuthorize("hasAnyRole('ADMIN') or #recipient == authentication.name")
    public ResponseEntity<Map<String, Object>> getNotificationStats(@PathVariable String recipient) {
        log.info("Getting notification stats for recipient: {}", recipient);
        
        Map<String, Object> stats = notificationService.getNotificationStats(recipient);
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping("/{notificationId}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> retryNotification(@PathVariable UUID notificationId) {
        log.info("Retrying notification: {}", notificationId);
        
        notificationService.processNotificationAsync(notificationId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification retry initiated");
        response.put("notificationId", notificationId.toString());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestParam String recipient,
            @RequestParam(defaultValue = "Test Email") String subject,
            @RequestParam(defaultValue = "This is a test email from the notification service.") String content) {
        
        log.info("Sending test email to: {}", recipient);
        
        Notification testNotification = Notification.builder()
                .type(com.ecommerce.notification.entity.NotificationType.PROMOTIONAL)
                .channel(com.ecommerce.notification.entity.NotificationChannel.EMAIL)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .build();
        
        notificationService.createNotification(testNotification);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test email sent");
        response.put("recipient", recipient);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-sms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendTestSms(
            @RequestParam String phoneNumber,
            @RequestParam(defaultValue = "Test SMS from notification service") String message) {
        
        log.info("Sending test SMS to: {}", phoneNumber);
        
        Notification testNotification = Notification.builder()
                .type(com.ecommerce.notification.entity.NotificationType.PROMOTIONAL)
                .channel(com.ecommerce.notification.entity.NotificationChannel.SMS)
                .recipient(phoneNumber)
                .content(message)
                .build();
        
        notificationService.createNotification(testNotification);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test SMS sent");
        response.put("phoneNumber", phoneNumber);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        return ResponseEntity.ok(health);
    }
}