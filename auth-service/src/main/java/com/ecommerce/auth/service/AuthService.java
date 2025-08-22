package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.UserCredential;
import com.ecommerce.auth.repository.UserCredentialRepository;
import com.ecommerce.auth.security.CustomUserDetails;
import com.ecommerce.common.utils.exception.BusinessException;
import com.ecommerce.common.utils.exception.ResourceNotFoundException;
import com.ecommerce.common.utils.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public AuthService(UserCredentialRepository userCredentialRepository,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      AuthenticationManager authenticationManager,
                      KafkaTemplate<String, Object> kafkaTemplate) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userCredentialRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USER_EXISTS", "Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userCredentialRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email already exists: " + request.getEmail());
        }

        // Create new user credential
        UserCredential userCredential = UserCredential.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserCredential.Role.CUSTOMER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        UserCredential savedUser = userCredentialRepository.save(userCredential);

        // Publish user registration event
        publishUserRegistrationEvent(savedUser);

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsernameOrEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserCredential userCredential = userDetails.getUserCredential();

        // Update last login time
        userCredentialRepository.updateLastLoginTime(userCredential.getId(), LocalDateTime.now());

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(userCredential);
        String refreshToken = jwtService.generateRefreshToken(userCredential);

        // Publish user login event
        publishUserLoginEvent(userCredential);

        log.info("User logged in successfully: {}", userCredential.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .userId(userCredential.getId())
                .username(userCredential.getUsername())
                .email(userCredential.getEmail())
                .role(userCredential.getRole().name())
                .build();
    }

    @Transactional
    public void initiatePasswordReset(PasswordResetRequest request) {
        log.info("Password reset initiated for email: {}", request.getEmail());

        UserCredential userCredential = userCredentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // 1 hour expiry

        userCredentialRepository.setResetToken(request.getEmail(), resetToken, expiresAt);

        // Publish password reset event
        publishPasswordResetEvent(userCredential, resetToken);

        log.info("Password reset token generated for user: {}", userCredential.getUsername());
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        log.info("Password reset confirmation with token: {}", request.getResetToken());

        UserCredential userCredential = userCredentialRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid or expired reset token"));

        // Check if token is expired
        if (userCredential.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("EXPIRED_TOKEN", "Reset token has expired");
        }

        // Reset password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userCredentialRepository.resetPassword(request.getResetToken(), encodedPassword);

        log.info("Password reset successfully for user: {}", userCredential.getUsername());
    }

    public boolean validateToken(String token) {
        try {
            return jwtService.validateToken(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    public UserDetailsResponse getUserById(String id) {
        log.debug("Fetching user details by ID: {}", id);
        
        UserCredential userCredential = userCredentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return UserDetailsResponse.builder()
                .id(userCredential.getId())
                .username(userCredential.getUsername())
                .email(userCredential.getEmail())
                .role(userCredential.getRole().name())
                .enabled(userCredential.isEnabled())
                .accountNonExpired(userCredential.isAccountNonExpired())
                .accountNonLocked(userCredential.isAccountNonLocked())
                .credentialsNonExpired(userCredential.isCredentialsNonExpired())
                .createdAt(userCredential.getCreatedAt())
                .lastLoginAt(userCredential.getLastLoginAt())
                .build();
    }

    private void publishUserRegistrationEvent(UserCredential userCredential) {
        try {
            kafkaTemplate.send(KafkaTopics.USER_REGISTRATION, userCredential.getId(), userCredential);
        } catch (Exception e) {
            log.error("Failed to publish user registration event", e);
        }
    }

    private void publishUserLoginEvent(UserCredential userCredential) {
        try {
            kafkaTemplate.send(KafkaTopics.USER_LOGIN, userCredential.getId(), userCredential);
        } catch (Exception e) {
            log.error("Failed to publish user login event", e);
        }
    }

    private void publishPasswordResetEvent(UserCredential userCredential, String resetToken) {
        try {
            kafkaTemplate.send(KafkaTopics.PASSWORD_RESET, userCredential.getId(), 
                    new PasswordResetEvent(userCredential.getEmail(), resetToken));
        } catch (Exception e) {
            log.error("Failed to publish password reset event", e);
        }
    }

    // Inner class for password reset event
    public static class PasswordResetEvent {
        private final String email;
        private final String resetToken;

        public PasswordResetEvent(String email, String resetToken) {
            this.email = email;
            this.resetToken = resetToken;
        }

        public String getEmail() { return email; }
        public String getResetToken() { return resetToken; }
    }
}