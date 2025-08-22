package com.ecommerce.user.controller;

import com.ecommerce.common.dto.user.AddressDTO;
import com.ecommerce.common.utils.response.ApiResponse;
import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.service.AddressService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/addresses")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);
    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(
            @PathVariable String userId,
            @Valid @RequestBody AddressCreateRequest request) {
        
        log.info("Creating address for user: {}", userId);
        
        AddressDTO address = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(address, "Address created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getUserAddresses(@PathVariable String userId) {
        log.debug("Fetching addresses for user: {}", userId);
        
        List<AddressDTO> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses, "Addresses retrieved successfully"));
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        
        log.debug("Fetching address {} for user: {}", addressId, userId);
        
        AddressDTO address = addressService.getAddressById(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(address, "Address retrieved successfully"));
    }

    @GetMapping("/default")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<AddressDTO>> getDefaultAddress(@PathVariable String userId) {
        log.debug("Fetching default address for user: {}", userId);
        
        AddressDTO address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.success(address, "Default address retrieved successfully"));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable String userId,
            @PathVariable String addressId,
            @Valid @RequestBody AddressCreateRequest request) {
        
        log.info("Updating address {} for user: {}", addressId, userId);
        
        AddressDTO address = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.success(address, "Address updated successfully"));
    }

    @PostMapping("/{addressId}/set-default")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        
        log.info("Setting address {} as default for user: {}", addressId, userId);
        
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address set as default successfully"));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAddressStats(@PathVariable String userId) {
        log.debug("Fetching address statistics for user: {}", userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAddresses", addressService.getAddressCount(userId));
        stats.put("hasDefaultAddress", addressService.hasDefaultAddress(userId));
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Address statistics retrieved successfully"));
    }
}