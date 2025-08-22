package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.common.utils.response.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());
        
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<String>> initiatePasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request received for email: {}", request.getEmail());
        
        authService.initiatePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset instructions sent to your email"));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<String>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        log.info("Password reset confirmation received");
        
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        log.debug("Token validation request received");
        
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid, 
                isValid ? "Token is valid" : "Token is invalid"));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUserById(@PathVariable String id) {
        log.debug("Get user by ID request received for: {}", id);
        
        UserDetailsResponse response = authService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User retrieved successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK", "Auth service is healthy"));
    }
}