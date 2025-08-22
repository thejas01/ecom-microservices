package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String requestId = request.getId();
        Instant startTime = Instant.now();

        logger.info("Request started - ID: {}, Method: {}, Path: {}, Client: {}", 
                requestId, method, path, request.getRemoteAddress());

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    Instant endTime = Instant.now();
                    long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
                    
                    logger.info("Request completed - ID: {}, Method: {}, Path: {}, Status: {}, Duration: {}ms", 
                            requestId, method, path, response.getStatusCode(), duration);
                })
        );
    }

    @Override
    public int getOrder() {
        return -100; // Execute first
    }
}