package com.ecommerce.user.service;

import com.ecommerce.common.dto.user.UserResponseDTO;
import com.ecommerce.common.utils.exception.BusinessException;
import com.ecommerce.common.utils.exception.ResourceNotFoundException;
import com.ecommerce.user.dto.UserCreateRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ecommerce.user.mapper.AddressMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = new UserMapper(new AddressMapper());

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateRequest createRequest;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(User.Gender.MALE)
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        createRequest = new UserCreateRequest();
        createRequest.setUsername("testuser");
        createRequest.setEmail("test@example.com");
        createRequest.setFirstName("Test");
        createRequest.setLastName("User");
        createRequest.setPhoneNumber("+1234567890");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        createRequest.setGender(User.Gender.MALE);

        responseDTO = new UserResponseDTO();
        responseDTO.setId("test-user-id");
        responseDTO.setUsername("testuser");
        responseDTO.setEmail("test@example.com");
        responseDTO.setFirstName("Test");
        responseDTO.setLastName("User");
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDTO result = userService.createUser(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.createUser(createRequest));
        
        assertEquals("USERNAME_EXISTS", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.createUser(createRequest));
        
        assertEquals("EMAIL_EXISTS", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.getUserById("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals("test-user-id", result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> userService.getUserById("non-existent-id"));
    }

    @Test
    void updateUser_Success() {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");

        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDTO result = userService.updateUser("test-user-id", updateRequest);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void updateUser_UsernameConflict_ThrowsException() {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername("newusername");

        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.updateUser("test-user-id", updateRequest));
        
        assertEquals("USERNAME_EXISTS", exception.getErrorCode());
    }

    @Test
    void deactivateUser_Success() {
        // Given
        testUser.setActive(true);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser("test-user-id");

        // Then
        assertFalse(testUser.isActive());
        verify(userRepository).save(testUser);
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void deactivateUser_AlreadyDeactivated_ThrowsException() {
        // Given
        testUser.setActive(false);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> userService.deactivateUser("test-user-id"));
        
        assertEquals("USER_ALREADY_DEACTIVATED", exception.getErrorCode());
    }

    @Test
    void verifyEmail_Success() {
        // Given
        testUser.setEmailVerified(false);
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.verifyEmail("test-user-id");

        // Then
        assertTrue(testUser.isEmailVerified());
        verify(userRepository).save(testUser);
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void existsByUsername_ReturnsTrue() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean exists = userService.existsByUsername("testuser");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // When
        boolean exists = userService.existsByEmail("test@example.com");

        // Then
        assertFalse(exists);
    }
}