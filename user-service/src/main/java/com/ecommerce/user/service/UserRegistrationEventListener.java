package com.ecommerce.user.service;

import com.ecommerce.common.utils.kafka.KafkaTopics;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.User.Gender;
import com.ecommerce.auth.entity.UserCredential;
import com.ecommerce.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserRegistrationEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(UserRegistrationEventListener.class);
    
    private final UserRepository userRepository;
    
    public UserRegistrationEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @KafkaListener(topics = KafkaTopics.USER_REGISTRATION, groupId = "user-service-group")
    @Transactional
    public void handleUserRegistration(@Payload UserCredential event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                     @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received user registration event: {} from topic: {}, partition: {}, offset: {}", 
                event, topic, partition, offset);
        
        try {
            // Check if user already exists
            if (userRepository.existsById(event.getId())) {
                log.warn("User with ID {} already exists. Skipping creation.", event.getId());
                return;
            }
            
            // Create user with minimal data from auth event
            User user = User.builder()
                    .id(event.getId())
                    .username(event.getUsername())
                    .email(event.getEmail())
                    .firstName("") // Will be updated when user completes profile
                    .lastName("")  // Will be updated when user completes profile
                    .gender(Gender.PREFER_NOT_TO_SAY) // Default value
                    .active(event.isEnabled())
                    .emailVerified(false) // Email verification will be handled separately
                    .phoneVerified(false)
                    .createdAt(event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            User savedUser = userRepository.save(user);
            log.info("Successfully created user profile for user ID: {} with username: {}", 
                    savedUser.getId(), savedUser.getUsername());
            
        } catch (Exception e) {
            log.error("Failed to create user profile for event: {}", event, e);
            // In production, you might want to:
            // 1. Send to a DLQ (Dead Letter Queue)
            // 2. Implement retry logic
            // 3. Send alert notifications
            throw new RuntimeException("Failed to process user registration event", e);
        }
    }
}