package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.*;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.notification.template.EmailTemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final EmailTemplateEngine templateEngine;
    
    @Transactional
    public Notification createNotification(Notification notification) {
        log.info("Creating notification - Type: {}, Channel: {}, Recipient: {}", 
                notification.getType(), notification.getChannel(), notification.getRecipient());
        
        // Validate notification
        validateNotification(notification);
        
        // Save notification
        Notification savedNotification = notificationRepository.save(notification);
        
        // Process notification asynchronously
        processNotificationAsync(savedNotification.getId());
        
        return savedNotification;
    }
    
    @Async
    public void processNotificationAsync(UUID notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            
            processNotification(notification);
        } catch (Exception e) {
            log.error("Error processing notification async: {}", notificationId, e);
        }
    }
    
    @Transactional
    public void processNotification(Notification notification) {
        log.info("Processing notification: {}", notification.getId());
        
        try {
            switch (notification.getChannel()) {
                case EMAIL:
                    sendEmailNotification(notification);
                    break;
                case SMS:
                    sendSmsNotification(notification);
                    break;
                case PUSH:
                    sendPushNotification(notification);
                    break;
                case IN_APP:
                    processInAppNotification(notification);
                    break;
                default:
                    log.error("Unsupported notification channel: {}", notification.getChannel());
                    updateNotificationStatus(notification, NotificationStatus.FAILED, 
                            "Unsupported notification channel");
            }
        } catch (Exception e) {
            log.error("Error processing notification: {}", notification.getId(), e);
            handleNotificationError(notification, e);
        }
    }
    
    private void sendEmailNotification(Notification notification) {
        log.info("Sending email notification: {}", notification.getId());
        
        try {
            String emailContent = notification.getContent();
            
            // If template is specified, process it
            if (notification.getTemplateName() != null) {
                Map<String, Object> variables = extractTemplateVariables(notification);
                emailContent = templateEngine.processTemplate(notification.getTemplateName(), variables);
            }
            
            // Send email
            if (emailContent.contains("<html>") || emailContent.contains("<body>")) {
                emailService.sendHtmlEmail(
                        notification.getRecipient(),
                        notification.getSubject(),
                        emailContent
                );
            } else {
                emailService.sendSimpleEmail(
                        notification.getRecipient(),
                        notification.getSubject(),
                        emailContent
                );
            }
            
            // Update notification status
            updateNotificationStatus(notification, NotificationStatus.SENT, null);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
    
    private void sendSmsNotification(Notification notification) {
        log.info("Sending SMS notification: {}", notification.getId());
        
        try {
            SmsService.SmsResponse response = smsService.sendSms(
                    notification.getRecipient(),
                    notification.getContent()
            );
            
            if (response.isSuccess()) {
                updateNotificationStatus(notification, NotificationStatus.SENT, null);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
            } else {
                throw new RuntimeException(response.getErrorMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS notification", e);
        }
    }
    
    private void sendPushNotification(Notification notification) {
        log.info("Sending push notification: {}", notification.getId());
        // Push notification implementation would go here
        log.warn("Push notifications not implemented yet");
        updateNotificationStatus(notification, NotificationStatus.FAILED, "Push notifications not implemented");
    }
    
    private void processInAppNotification(Notification notification) {
        log.info("Processing in-app notification: {}", notification.getId());
        // In-app notifications would typically be stored and retrieved by the app
        updateNotificationStatus(notification, NotificationStatus.SENT, null);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    private void handleNotificationError(Notification notification, Exception e) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        
        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            updateNotificationStatus(notification, NotificationStatus.FAILED, e.getMessage());
        } else {
            updateNotificationStatus(notification, NotificationStatus.PENDING, 
                    "Retry " + notification.getRetryCount() + ": " + e.getMessage());
        }
        
        notificationRepository.save(notification);
    }
    
    private void updateNotificationStatus(Notification notification, NotificationStatus status, String errorMessage) {
        notification.setStatus(status);
        if (errorMessage != null) {
            notification.setErrorMessage(errorMessage);
        }
    }
    
    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void retryFailedNotifications() {
        log.debug("Checking for failed notifications to retry");
        
        List<Notification> failedNotifications = notificationRepository.findRetriableNotifications(NotificationStatus.FAILED);
        
        for (Notification notification : failedNotifications) {
            log.info("Retrying notification: {}", notification.getId());
            processNotification(notification);
        }
    }
    
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void processStaleNotifications() {
        log.debug("Checking for stale notifications");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
        List<Notification> staleNotifications = notificationRepository.findStaleNotifications(cutoffTime);
        
        for (Notification notification : staleNotifications) {
            log.warn("Found stale notification: {}", notification.getId());
            updateNotificationStatus(notification, NotificationStatus.FAILED, "Notification timeout");
            notificationRepository.save(notification);
        }
    }
    
    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Starting cleanup of old notifications");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<NotificationStatus> statusesToDelete = Arrays.asList(
                NotificationStatus.SENT, NotificationStatus.DELIVERED, NotificationStatus.FAILED
        );
        
        notificationRepository.deleteByCreatedAtBeforeAndStatusIn(cutoffDate, statusesToDelete);
        log.info("Completed cleanup of old notifications");
    }
    
    private void validateNotification(Notification notification) {
        if (notification.getRecipient() == null || notification.getRecipient().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        
        if (notification.getChannel() == null) {
            throw new IllegalArgumentException("Notification channel is required");
        }
        
        if (notification.getType() == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        
        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            if (notification.getTemplateName() == null || notification.getTemplateName().trim().isEmpty()) {
                throw new IllegalArgumentException("Either content or template name is required");
            }
        }
        
        // Validate recipient based on channel
        switch (notification.getChannel()) {
            case EMAIL:
                if (!emailService.validateEmailAddress(notification.getRecipient())) {
                    throw new IllegalArgumentException("Invalid email address");
                }
                break;
            case SMS:
                if (!smsService.isValidPhoneNumber(notification.getRecipient())) {
                    throw new IllegalArgumentException("Invalid phone number");
                }
                break;
        }
    }
    
    private Map<String, Object> extractTemplateVariables(Notification notification) {
        Map<String, Object> variables = new HashMap<>();
        
        // Add basic notification data
        variables.put("recipient", notification.getRecipient());
        variables.put("notificationType", notification.getType());
        
        // Add reference data if available
        if (notification.getReferenceId() != null) {
            variables.put("referenceId", notification.getReferenceId());
            variables.put("referenceType", notification.getReferenceType());
        }
        
        // Parse additional data from content if it's JSON
        if (notification.getContent() != null && notification.getContent().startsWith("{")) {
            try {
                // Simple JSON parsing - in production, use Jackson or Gson
                // This is a placeholder for actual JSON parsing
                variables.put("data", notification.getContent());
            } catch (Exception e) {
                log.warn("Failed to parse notification content as JSON", e);
            }
        }
        
        return variables;
    }
    
    // Query methods
    
    public Notification getNotification(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }
    
    public Page<Notification> getNotificationsByRecipient(String recipient, Pageable pageable) {
        return notificationRepository.findByRecipient(recipient, pageable);
    }
    
    public List<Notification> getNotificationsByReference(String referenceId, String referenceType) {
        return notificationRepository.findByReferenceIdAndReferenceType(referenceId, referenceType);
    }
    
    public Long getUnreadCount(String recipient) {
        return notificationRepository.countByRecipientAndStatus(recipient, NotificationStatus.SENT);
    }
    
    public Map<String, Object> getNotificationStats(String recipient) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", notificationRepository.countByRecipientAndStatus(recipient, null));
        stats.put("sent", notificationRepository.countByRecipientAndStatus(recipient, NotificationStatus.SENT));
        stats.put("failed", notificationRepository.countByRecipientAndStatus(recipient, NotificationStatus.FAILED));
        stats.put("pending", notificationRepository.countByRecipientAndStatus(recipient, NotificationStatus.PENDING));
        
        return stats;
    }
}