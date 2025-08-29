package com.ecommerce.payment.repository;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByCustomerId(String customerId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCustomerIdAndStatus(String customerId, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :expiryTime")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status, @Param("expiryTime") LocalDateTime expiryTime);

    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByCustomerIdAndDateRange(
        @Param("customerId") String customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
    BigDecimal getTotalAmountByCustomerIdAndStatus(
        @Param("customerId") String customerId,
        @Param("status") PaymentStatus status
    );

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate")
    Long countByStatusSince(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    boolean existsByOrderIdAndStatusIn(String orderId, List<PaymentStatus> statuses);
}