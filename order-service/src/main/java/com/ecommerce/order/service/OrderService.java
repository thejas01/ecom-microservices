package com.ecommerce.order.service;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating new order for customer: {}", order.getCustomerId());
        
        // Generate order number
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        
        // Establish bidirectional relationships for all items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
                // Calculate total price for each item
                item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        
        // Calculate subtotal from items
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);
        
        // Save order with items (cascade)
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} and order number: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(String orderId) {
        log.debug("Fetching order by ID: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersByCustomerId(String customerId, Pageable pageable) {
        log.debug("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to: {}", orderId, newStatus);
        
        Order order = getOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // Set timestamp based on status
        switch (newStatus) {
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                break;
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", orderId, previousStatus, newStatus);
        
        return updatedOrder;
    }

    @Transactional
    public Order updatePaymentInfo(String orderId, String paymentId, String paymentStatus) {
        log.info("Updating payment info for order: {}", orderId);
        
        Order order = getOrderById(orderId);
        order.setPaymentId(paymentId);
        order.setPaymentStatus(paymentStatus);
        
        if ("COMPLETED".equalsIgnoreCase(paymentStatus)) {
            order.setStatus(OrderStatus.PAYMENT_COMPLETED);
        } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(String orderId, String reason) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = getOrderById(orderId);
        
        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setNotes(reason);
        
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order {} cancelled successfully", orderId);
        
        return cancelledOrder;
    }

    @Transactional
    public Order addOrderItem(String orderId, OrderItem item) {
        log.info("Adding item to order: {}", orderId);
        
        Order order = getOrderById(orderId);
        
        // Check if order can be modified
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot modify order in status: " + order.getStatus());
        }
        
        order.addItem(item);
        
        // Recalculate totals
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);
        
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Long getOrderCountByCustomer(String customerId) {
        return orderRepository.countOrdersByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getStaleOrders(OrderStatus status, int hours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return orderRepository.findStaleOrdersByStatusAndCreatedBefore(status, threshold);
    }

    private String generateOrderNumber() {
        // Generate unique order number with timestamp and random UUID
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }
}