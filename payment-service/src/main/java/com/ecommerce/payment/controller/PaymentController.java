package com.ecommerce.payment.controller;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment, Authentication authentication) {
        log.info("Creating payment for order: {} by user: {}", payment.getOrderId(), authentication.getName());
        
        // Set customer ID from authentication if not provided
        if (payment.getCustomerId() == null || payment.getCustomerId().isEmpty()) {
            payment.setCustomerId(authentication.getName());
        }

        Payment createdPayment = paymentService.createPayment(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<Payment> processPayment(@PathVariable String paymentId) {
        log.info("Processing payment: {}", paymentId);
        Payment processedPayment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(processedPayment);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<Payment> refundPayment(
            @PathVariable String paymentId,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal refundAmount = request.get("amount");
        log.info("Refunding payment: {} amount: {}", paymentId, refundAmount);
        
        Payment refundedPayment = paymentService.refundPayment(paymentId, refundAmount);
        return ResponseEntity.ok(refundedPayment);
    }

    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<Payment> cancelPayment(@PathVariable String paymentId, Authentication authentication) {
        log.info("Cancelling payment: {} by user: {}", paymentId, authentication.getName());
        
        Payment payment = paymentService.getPayment(paymentId);
        
        // Check if user owns the payment or is admin
        if (!payment.getCustomerId().equals(authentication.getName()) && 
            !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Payment cancelledPayment = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(cancelledPayment);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId, Authentication authentication) {
        Payment payment = paymentService.getPayment(paymentId);
        
        // Check if user owns the payment or is admin
        if (!payment.getCustomerId().equals(authentication.getName()) && 
            !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId, Authentication authentication) {
        return paymentService.getPaymentByOrderId(orderId)
            .map(payment -> {
                // Check if user owns the payment or is admin
                if (!payment.getCustomerId().equals(authentication.getName()) && 
                    !authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SYSTEM"))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).<Payment>build();
                }
                return ResponseEntity.ok(payment);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsByCustomer(
            @PathVariable String customerId,
            Authentication authentication) {
        
        // Users can only view their own payments unless they're admin
        if (!customerId.equals(authentication.getName()) && 
            !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Payment> payments = paymentService.getPaymentsByCustomerId(customerId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}/status")
    @PreAuthorize("hasAnyRole('USER', 'CUSTOMER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        
        paymentService.getPaymentByOrderId(orderId).ifPresentOrElse(
            payment -> {
                response.put("exists", true);
                response.put("status", payment.getStatus());
                response.put("paymentId", payment.getId());
                response.put("amount", payment.getAmount());
                response.put("currency", payment.getCurrency());
            },
            () -> {
                response.put("exists", false);
                response.put("message", "No payment found for order");
            }
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/expire-old")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> expireOldPayments(
            @RequestParam(defaultValue = "30") int expiryMinutes) {
        paymentService.expireOldPayments(expiryMinutes);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Old payments expired successfully");
        response.put("expiryMinutes", String.valueOf(expiryMinutes));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "payment-service");
        return ResponseEntity.ok(health);
    }
}