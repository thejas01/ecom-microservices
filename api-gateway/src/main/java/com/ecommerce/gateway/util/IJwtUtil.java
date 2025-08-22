package com.ecommerce.gateway.util;

public interface IJwtUtil {
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    String getUserIdFromToken(String token);
    String getRoleFromToken(String token);
}