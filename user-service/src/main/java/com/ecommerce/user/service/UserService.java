package com.ecommerce.user.service;

import com.ecommerce.common.dto.user.UserDTO;
import com.ecommerce.common.dto.user.UserResponseDTO;
import com.ecommerce.common.utils.exception.BusinessException;
import com.ecommerce.common.utils.exception.ResourceNotFoundException;
import com.ecommerce.common.utils.kafka.KafkaTopics;
import com.ecommerce.user.dto.UserCreateRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, 
                      KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        validateUserUniqueness(request.getUsername(), request.getEmail());

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        publishUserEvent("USER_CREATED", savedUser);
        return userMapper.toUserResponseDTO(savedUser);
    }

    //@Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = findUserById(id);
        return userMapper.toUserResponseDTO(user);
    }

    //@Cacheable(value = "users", key = "#username")
    public UserResponseDTO getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toUserResponseDTO(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponseDTO(user);
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all active users with pagination");
        Page<User> users = userRepository.findByActiveTrue(pageable);
        return users.map(userMapper::toUserResponseDTO);
    }

    public Page<UserResponseDTO> searchUsers(String search, Pageable pageable) {
        log.debug("Searching users with term: {}", search);
        Page<User> users = userRepository.searchActiveUsers(search, pageable);
        return users.map(userMapper::toUserResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDTO updateUser(String id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        
        User user = findUserById(id);
        User originalUser = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        boolean updated = false;

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("Username already exists", "USERNAME_EXISTS", HttpStatus.CONFLICT);
            }
            user.setUsername(request.getUsername());
            updated = true;
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already exists", "EMAIL_EXISTS", HttpStatus.CONFLICT);
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
            updated = true;
        }

        if (request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
            updated = true;
        }

        if (request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
            updated = true;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPhoneVerified(false);
            updated = true;
        }

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().equals(user.getDateOfBirth())) {
            user.setDateOfBirth(request.getDateOfBirth());
            updated = true;
        }

        if (request.getGender() != null && !request.getGender().equals(user.getGender())) {
            user.setGender(request.getGender());
            updated = true;
        }

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().equals(user.getProfileImageUrl())) {
            user.setProfileImageUrl(request.getProfileImageUrl());
            updated = true;
        }

        if (updated) {
            User savedUser = userRepository.save(user);
            log.info("User updated successfully: {}", savedUser.getId());
            publishUserUpdateEvent(originalUser, savedUser);
            return userMapper.toUserResponseDTO(savedUser);
        } else {
            log.debug("No changes detected for user: {}", id);
            return userMapper.toUserResponseDTO(user);
        }
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deactivateUser(String id) {
        log.info("Deactivating user with ID: {}", id);
        User user = findUserById(id);
        
        if (!user.isActive()) {
            throw new BusinessException("User is already deactivated", "USER_ALREADY_DEACTIVATED", HttpStatus.BAD_REQUEST);
        }
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", id);
        publishUserEvent("USER_DEACTIVATED", user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void activateUser(String id) {
        log.info("Activating user with ID: {}", id);
        User user = findUserById(id);
        
        if (user.isActive()) {
            throw new BusinessException("User is already active", "USER_ALREADY_ACTIVE", HttpStatus.BAD_REQUEST);
        }
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("User activated successfully: {}", id);
        publishUserEvent("USER_ACTIVATED", user);
    }

    @Transactional
    public void updateLastLogin(String id) {
        log.debug("Updating last login for user: {}", id);
        User user = findUserById(id);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void verifyEmail(String id) {
        log.info("Verifying email for user: {}", id);
        User user = findUserById(id);
        user.setEmailVerified(true);
        userRepository.save(user);
        publishUserEvent("EMAIL_VERIFIED", user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void verifyPhone(String id) {
        log.info("Verifying phone for user: {}", id);
        User user = findUserById(id);
        user.setPhoneVerified(true);
        userRepository.save(user);
        publishUserEvent("PHONE_VERIFIED", user);
    }

    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private void validateUserUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Username already exists", "USERNAME_EXISTS", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists", "EMAIL_EXISTS", HttpStatus.CONFLICT);
        }
    }

    private void publishUserEvent(String eventType, User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("userId", user.getId());
            event.put("username", user.getUsername());
            event.put("email", user.getEmail());
            event.put("timestamp", LocalDateTime.now().toString());
            
            kafkaTemplate.send(KafkaTopics.USER_EVENTS_TOPIC, user.getId(), event);
            log.debug("Published {} event for user: {}", eventType, user.getId());
        } catch (Exception e) {
            log.error("Failed to publish {} event for user: {}", eventType, user.getId(), e);
        }
    }

    private void publishUserUpdateEvent(User originalUser, User updatedUser) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "USER_UPDATED");
            event.put("userId", updatedUser.getId());
            event.put("originalData", userMapper.toUserDTO(originalUser));
            event.put("updatedData", userMapper.toUserDTO(updatedUser));
            event.put("timestamp", LocalDateTime.now().toString());
            
            kafkaTemplate.send(KafkaTopics.USER_EVENTS_TOPIC, updatedUser.getId(), event);
            log.debug("Published USER_UPDATED event for user: {}", updatedUser.getId());
        } catch (Exception e) {
            log.error("Failed to publish USER_UPDATED event for user: {}", updatedUser.getId(), e);
        }
    }
}