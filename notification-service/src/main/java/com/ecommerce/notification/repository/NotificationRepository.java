package com.ecommerce.notification.repository;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationChannel;
import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByRecipient(String recipient);
    
    Page<Notification> findByRecipient(String recipient, Pageable pageable);
    
    List<Notification> findByStatus(NotificationStatus status);
    
    List<Notification> findByStatusAndChannel(NotificationStatus status, NotificationChannel channel);
    
    List<Notification> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
    
    Optional<Notification> findByReferenceIdAndReferenceTypeAndType(
            String referenceId, String referenceType, NotificationType type);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries")
    List<Notification> findRetriableNotifications(@Param("status") NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.createdAt < :cutoffTime")
    List<Notification> findStaleNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.status = :status")
    Long countByRecipientAndStatus(@Param("recipient") String recipient, @Param("status") NotificationStatus status);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type AND n.sentAt >= :startDate AND n.sentAt <= :endDate")
    Long countByTypeAndDateRange(
            @Param("type") NotificationType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    List<Notification> findByRecipientAndStatusOrderByCreatedAtDesc(
            String recipient, NotificationStatus status);
    
    Page<Notification> findByTypeAndCreatedAtBetween(
            NotificationType type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    void deleteByCreatedAtBeforeAndStatusIn(LocalDateTime cutoffDate, List<NotificationStatus> statuses);
}