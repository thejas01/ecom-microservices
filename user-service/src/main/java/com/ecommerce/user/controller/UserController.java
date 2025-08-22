package com.ecommerce.user.controller;

import com.ecommerce.common.dto.user.UserResponseDTO;
import com.ecommerce.common.utils.response.ApiResponse;
import com.ecommerce.common.utils.response.PageInfo;
import com.ecommerce.user.dto.UserCreateRequest;
import com.ecommerce.user.dto.UserUpdateRequest;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());
        
        UserResponseDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable String id) {
        log.debug("Fetching user by ID: {}", id);
        
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserByUsername(@PathVariable String username) {
        log.debug("Fetching user by username: {}", username);
        
        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserByEmail(@PathVariable String email) {
        log.debug("Fetching user by email: {}", email);
        
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Fetching all users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        
        PageInfo pageInfo = PageInfo.builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .hasNext(users.hasNext())
                .hasPrevious(users.hasPrevious())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(users.getContent(), "Users retrieved successfully", pageInfo));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Searching users with query: {} - page: {}, size: {}", query, page, size);
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponseDTO> users = userService.searchUsers(query, pageable);
        
        PageInfo pageInfo = PageInfo.builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .hasNext(users.hasNext())
                .hasPrevious(users.hasPrevious())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(users.getContent(), "Search results retrieved successfully", pageInfo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("Updating user with ID: {}", id);
        
        UserResponseDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable String id) {
        log.info("Deactivating user with ID: {}", id);
        
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable String id) {
        log.info("Activating user with ID: {}", id);
        
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User activated successfully"));
    }

    @PostMapping("/{id}/verify-email")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String id) {
        log.info("Verifying email for user: {}", id);
        
        userService.verifyEmail(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
    }

    @PostMapping("/{id}/verify-phone")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@PathVariable String id) {
        log.info("Verifying phone for user: {}", id);
        
        userService.verifyPhone(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Phone verified successfully"));
    }

    @PostMapping("/{id}/last-login")
    public ResponseEntity<ApiResponse<Void>> updateLastLogin(@PathVariable String id) {
        log.debug("Updating last login for user: {}", id);
        
        userService.updateLastLogin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Last login updated successfully"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        log.debug("Fetching user statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUserCount", userService.getActiveUserCount());
        
        return ResponseEntity.ok(ApiResponse.success(stats, "User statistics retrieved successfully"));
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@PathVariable String username) {
        log.debug("Checking availability of username: {}", username);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", userService.existsByUsername(username));
        
        return ResponseEntity.ok(ApiResponse.success(result, "Username availability checked"));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@PathVariable String email) {
        log.debug("Checking availability of email: {}", email);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", userService.existsByEmail(email));
        
        return ResponseEntity.ok(ApiResponse.success(result, "Email availability checked"));
    }
}