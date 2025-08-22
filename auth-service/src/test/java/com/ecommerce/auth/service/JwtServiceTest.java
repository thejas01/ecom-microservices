package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.UserCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserCredential testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "ThisIsAVeryLongSecretKeyThatIsAtLeast256BitsLongForTesting");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        testUser = UserCredential.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .role(UserCredential.Role.CUSTOMER)
                .build();
    }

    @Test
    void shouldGenerateAccessToken() {
        String token = jwtService.generateAccessToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void shouldGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtService.generateAccessToken(testUser);
        
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateAccessToken(testUser);
        
        String username = jwtService.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtService.generateAccessToken(testUser);
        
        String userId = jwtService.getUserIdFromToken(token);
        assertEquals("test-user-id", userId);
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtService.generateAccessToken(testUser);
        
        String role = jwtService.getRoleFromToken(token);
        assertEquals("CUSTOMER", role);
    }

    @Test
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateAccessToken(testUser);
        
        String email = jwtService.getEmailFromToken(token);
        assertEquals("test@example.com", email);
    }

    @Test
    void shouldIdentifyRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        String accessToken = jwtService.generateAccessToken(testUser);
        
        assertTrue(jwtService.isRefreshToken(refreshToken));
        assertFalse(jwtService.isRefreshToken(accessToken));
    }
}