package com.ecommerce.user.controller;

import com.ecommerce.common.dto.user.UserResponseDTO;
import com.ecommerce.user.dto.UserCreateRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.ecommerce.user.config.SecurityTestConfig;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(SecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private com.ecommerce.common.utils.security.JwtTokenUtil jwtTokenUtil;

    private UserResponseDTO testUserResponse;
    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponseDTO();
        testUserResponse.setId("test-user-id");
        testUserResponse.setUsername("testuser");
        testUserResponse.setEmail("test@example.com");
        testUserResponse.setFirstName("Test");
        testUserResponse.setLastName("User");

        createRequest = new UserCreateRequest();
        createRequest.setUsername("testuser");
        createRequest.setEmail("test@example.com");
        createRequest.setFirstName("Test");
        createRequest.setLastName("User");
        createRequest.setGender(User.Gender.MALE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithAdminRole_Success() throws Exception {
        // Given
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(post("/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createUser_WithoutAdminRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void getUserById_OwnProfile_Success() throws Exception {
        // Given
        when(userService.getUserById("test-user-id")).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/users/test-user-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("test-user-id"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByUsername_WithAdminRole_Success() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithPagination_Success() throws Exception {
        // Given
        Page<UserResponseDTO> page = new PageImpl<>(
            Collections.singletonList(testUserResponse),
            PageRequest.of(0, 20),
            1
        );
        when(userService.getAllUsers(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/users")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("testuser"))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(0))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_Success() throws Exception {
        // Given
        Page<UserResponseDTO> page = new PageImpl<>(
            Collections.singletonList(testUserResponse),
            PageRequest.of(0, 20),
            1
        );
        when(userService.searchUsers(anyString(), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/users/search")
                .param("query", "test")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void updateUser_OwnProfile_Success() throws Exception {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        
        when(userService.updateUser(eq("test-user-id"), any())).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(put("/users/test-user-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_WithAdminRole_Success() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser("test-user-id");

        // When & Then
        mockMvc.perform(post("/users/test-user-id/deactivate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));

        verify(userService).deactivateUser("test-user-id");
    }

    @Test
    void checkUsername_PublicEndpoint_Success() throws Exception {
        // Given
        when(userService.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/users/check-username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    void checkEmail_PublicEndpoint_Success() throws Exception {
        // Given
        when(userService.existsByEmail("test@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/users/check-email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserStats_WithAdminRole_Success() throws Exception {
        // Given
        when(userService.getActiveUserCount()).thenReturn(100L);

        // When & Then
        mockMvc.perform(get("/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeUserCount").value(100));
    }

    @Test
    @WithMockUser
    void createUser_InvalidRequest_BadRequest() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}