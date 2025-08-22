package com.ecommerce.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth-service")
    public Mono<ResponseEntity<Map<String, Object>>> authServiceFallback() {
        return createFallbackResponse("Auth Service");
    }

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return createFallbackResponse("User Service");
    }

    @GetMapping("/product-catalog-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return createFallbackResponse("Product Catalog Service");
    }

    @GetMapping("/inventory-service")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryServiceFallback() {
        return createFallbackResponse("Inventory Service");
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        return createFallbackResponse("Order Service");
    }

    @GetMapping("/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        return createFallbackResponse("Payment Service");
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return createFallbackResponse("Notification Service");
    }

    private Mono<ResponseEntity<Map<String, Object>>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is temporarily unavailable. Please try again later.");
        response.put("service", serviceName);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}