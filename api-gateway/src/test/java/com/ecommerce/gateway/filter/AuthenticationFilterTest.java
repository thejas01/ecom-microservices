package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.util.IJwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private IJwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    private AuthenticationFilter.Config config;

    @BeforeEach
    void setUp() {
        config = new AuthenticationFilter.Config();
    }

    @Test
    void testMissingAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        MockServerHttpResponse response = exchange.getResponse();

        StepVerifier.create(authenticationFilter.apply(config).filter(exchange, filterChain))
                .verifyComplete();

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void testInvalidTokenFormat() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "InvalidToken")
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        MockServerHttpResponse response = exchange.getResponse();

        StepVerifier.create(authenticationFilter.apply(config).filter(exchange, filterChain))
                .verifyComplete();

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void testValidToken() {
        String token = "validToken";
        String username = "testuser";
        String userId = "123";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.apply(config).filter(exchange, filterChain))
                .verifyComplete();

        verify(jwtUtil).validateToken(token);
        verify(filterChain).filter(any(ServerWebExchange.class));
    }

    @Test
    void testInvalidToken() {
        String token = "invalidToken";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        MockServerHttpResponse response = exchange.getResponse();
        when(jwtUtil.validateToken(token)).thenReturn(false);

        StepVerifier.create(authenticationFilter.apply(config).filter(exchange, filterChain))
                .verifyComplete();

        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(jwtUtil).validateToken(token);
    }
}