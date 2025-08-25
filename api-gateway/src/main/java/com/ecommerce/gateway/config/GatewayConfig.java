package com.ecommerce.gateway.config;

import com.ecommerce.gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - Public routes
                .route("auth-service-public", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.rewritePath("/api/auth/(?<path>.*)", "/auth/${path}"))
                        .uri("lb://AUTH-SERVICE"))
                
                // User Service - Protected routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .rewritePath("/api/users/(?<path>.*)", "/users/${path}")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE"))
                
                // Product Catalog Service - Public GET routes
                .route("product-catalog-service-public", r -> r
                        .path("/api/products/**", "/api/categories/**")
                        .and()
                        .method("GET")
                        .filters(f -> f.rewritePath("/api/(?<path>.*)", "/api/${path}"))
                        .uri("lb://PRODUCT-CATALOG-SERVICE"))
                
                // Product Catalog Service - Protected POST/PUT/DELETE routes  
                .route("product-catalog-service-protected", r -> r
                        .path("/api/products/**")
                        .and()
                        .method("POST", "PUT", "DELETE", "PATCH")
                        .filters(f -> f
                                .rewritePath("/api/(?<path>.*)", "/api/${path}")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://PRODUCT-CATALOG-SERVICE"))
                
                // Inventory Service - Public health endpoint
                .route("inventory-service-health", r -> r
                        .path("/api/inventory/health")
                        .filters(f -> f
                                .rewritePath("/api/inventory/(?<path>.*)", "/inventory/${path}"))
                        .uri("lb://INVENTORY-SERVICE"))
                
                // Inventory Service - Protected routes
                .route("inventory-service", r -> r
                        .path("/api/inventory", "/api/inventory/**")
                        .filters(f -> f
                                .rewritePath("/api/inventory(?<path>/.*)?", "/inventory${path}")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://INVENTORY-SERVICE"))
                
                // Order Service - Protected routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .rewritePath("/api/orders/(?<path>.*)", "/orders/${path}")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://ORDER-SERVICE"))
                
                // Payment Service - Protected routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .rewritePath("/api/payments/(?<path>.*)", "/payments/${path}")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://PAYMENT-SERVICE"))
                
                // Notification Service - Protected routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .rewritePath("/api/notifications/(?<path>.*)", "/notifications/${path}")
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://NOTIFICATION-SERVICE"))
                
                .build();
    }
}