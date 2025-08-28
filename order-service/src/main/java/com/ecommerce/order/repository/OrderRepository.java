package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status = :status")
    List<Order> findByCustomerIdAndStatus(@Param("customerId") String customerId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    Long countOrdersByCustomerId(@Param("customerId") String customerId);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :timestamp")
    List<Order> findStaleOrdersByStatusAndCreatedBefore(@Param("status") OrderStatus status, @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT o FROM Order o WHERE o.paymentId = :paymentId")
    Optional<Order> findByPaymentId(@Param("paymentId") String paymentId);

    boolean existsByOrderNumber(String orderNumber);
}