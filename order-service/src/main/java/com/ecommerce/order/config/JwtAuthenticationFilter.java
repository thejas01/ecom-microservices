package com.ecommerce.order.config;

import com.ecommerce.common.utils.security.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // First check for API Gateway headers
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String userRole = request.getHeader("X-User-Role");
        
        if (userId != null && username != null && userRole != null) {
            log.debug("Found API Gateway headers - User: {}, Role: {}", username, userRole);
            setAuthentication(request, username, userRole);
        } else {
            // Fallback to direct JWT validation
            final String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                
                try {
                    if (jwtTokenUtil.validateToken(token)) {
                        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
                        String extractedRole = jwtTokenUtil.getRoleFromToken(token);
                        
                        log.debug("Valid JWT token - User: {}, Role: {}", extractedUsername, extractedRole);
                        setAuthentication(request, extractedUsername, extractedRole);
                    }
                } catch (Exception e) {
                    log.error("JWT token validation failed: {}", e.getMessage());
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void setAuthentication(HttpServletRequest request, String username, String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username, null, Collections.singletonList(authority));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}