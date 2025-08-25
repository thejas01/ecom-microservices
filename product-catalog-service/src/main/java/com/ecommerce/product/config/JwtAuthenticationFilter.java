package com.ecommerce.product.config;

import com.ecommerce.common.utils.security.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // First check if request is coming from API Gateway with pre-authenticated headers
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String role = request.getHeader("X-User-Role");
        
        if (userId != null && username != null && role != null) {
            // Trust the API Gateway's authentication
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    new UserPrincipal(userId, username, role),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set authentication from gateway headers for user: {} with role: {}", username, role);
        } else {
            // Fallback to direct JWT validation if not from gateway
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                try {
                    if (jwtTokenUtil.validateToken(token)) {
                        username = jwtTokenUtil.getUsernameFromToken(token);
                        userId = jwtTokenUtil.getUserIdFromToken(token);
                        role = jwtTokenUtil.getRoleFromToken(token);
                        
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                new UserPrincipal(userId, username, role),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Set authentication from JWT for user: {} with role: {}", username, role);
                    }
                } catch (Exception e) {
                    log.error("JWT validation failed: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    public static class UserPrincipal {
        private final String userId;
        private final String username;
        private final String role;

        public UserPrincipal(String userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}