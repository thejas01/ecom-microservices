package com.ecommerce.order.controller;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderOrchestrationService;
import com.ecommerce.order.service.OrderService;
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderOrchestrationService orderOrchestrationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody Order order,
            @RequestHeader("Authorization") String authToken,
            Authentication authentication) {
        log.info("Creating order for customer: {}", authentication.getName());
        
        // Set customer ID from authentication
        order.setCustomerId(authentication.getName());
        
        // Process order through orchestration service
        Order createdOrder = orderOrchestrationService.processOrder(order, authToken);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> getOrderById(
            @PathVariable String orderId,
            Authentication authentication) {
        log.info("Fetching order: {} for user: {}", orderId, authentication.getName());
        
        Order order = orderService.getOrderById(orderId);
        
        // Check if user is authorized to view this order
        if (!isUserAuthorized(order, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }

    @GetMapping("/order-number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            Authentication authentication) {
        log.info("Fetching order by order number: {} for user: {}", orderNumber, authentication.getName());
        
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        
        // Check if user is authorized to view this order
        if (!isUserAuthorized(order, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN') or #customerId == authentication.name")
    public ResponseEntity<Page<Order>> getCustomerOrders(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("Fetching orders for customer: {}", customerId);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Order> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Order>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {
        
        String customerId = authentication.getName();
        log.info("Fetching orders for authenticated customer: {}", customerId);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Order> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        log.info("Updating order {} status to: {}", orderId, status);
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable String orderId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Cancelling order: {} by user: {}", orderId, authentication.getName());
        
        Order order = orderService.getOrderById(orderId);
        
        // Check if user is authorized to cancel this order
        if (!isUserAuthorized(order, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String cancellationReason = reason != null ? reason : "Cancelled by customer";
        Order cancelledOrder = orderService.cancelOrder(orderId, cancellationReason);
        
        return ResponseEntity.ok(cancelledOrder);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/count/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN') or #customerId == authentication.name")
    public ResponseEntity<Map<String, Object>> getCustomerOrderCount(@PathVariable String customerId) {
        log.info("Getting order count for customer: {}", customerId);
        
        Long count = orderService.getOrderCountByCustomer(customerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("customerId", customerId);
        response.put("orderCount", count);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("Fetching all orders - page: {}, size: {}", page, size);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Order> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "order-service");
        return ResponseEntity.ok(health);
    }

    private boolean isUserAuthorized(Order order, Authentication authentication) {
        // Admin can access all orders
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // Customer can only access their own orders
        return order.getCustomerId().equals(authentication.getName());
    }
}