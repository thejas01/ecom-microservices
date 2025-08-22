package com.ecommerce.user.service;

import com.ecommerce.common.dto.user.AddressDTO;
import com.ecommerce.common.utils.exception.BusinessException;
import com.ecommerce.common.utils.exception.ResourceNotFoundException;
import com.ecommerce.common.utils.kafka.KafkaTopics;
import com.ecommerce.user.dto.AddressCreateRequest;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.mapper.AddressMapper;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public AddressService(AddressRepository addressRepository, UserRepository userRepository,
                         AddressMapper addressMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public AddressDTO createAddress(String userId, AddressCreateRequest request) {
        log.info("Creating address for user: {}", userId);
        
        User user = findUserById(userId);
        
        Address address = Address.builder()
                .user(user)
                .type(request.getType())
                .label(request.getLabel())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();

        // If this is set as default, unset all other default addresses for this user
        if (request.isDefault()) {
            addressRepository.unsetAllDefaultAddresses(userId);
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with ID: {}", savedAddress.getId());
        
        publishAddressEvent("ADDRESS_CREATED", userId, savedAddress);
        return addressMapper.toAddressDTO(savedAddress);
    }

    public List<AddressDTO> getUserAddresses(String userId) {
        log.debug("Fetching addresses for user: {}", userId);
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtAsc(userId);
        return addresses.stream()
                .map(addressMapper::toAddressDTO)
                .toList();
    }

    public AddressDTO getAddressById(String userId, String addressId) {
        log.debug("Fetching address {} for user: {}", addressId, userId);
        Address address = findAddressByIdAndUserId(addressId, userId);
        return addressMapper.toAddressDTO(address);
    }

    public AddressDTO getDefaultAddress(String userId) {
        log.debug("Fetching default address for user: {}", userId);
        Address address = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found for user: " + userId));
        return addressMapper.toAddressDTO(address);
    }

    @Transactional
    public AddressDTO updateAddress(String userId, String addressId, AddressCreateRequest request) {
        log.info("Updating address {} for user: {}", addressId, userId);
        
        Address address = findAddressByIdAndUserId(addressId, userId);
        
        address.setType(request.getType());
        address.setLabel(request.getLabel());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        // Handle default address logic
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.unsetDefaultAddressesExcept(userId, addressId);
            address.setDefault(true);
        } else if (!request.isDefault() && address.isDefault()) {
            address.setDefault(false);
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address updated successfully: {}", addressId);
        
        publishAddressEvent("ADDRESS_UPDATED", userId, savedAddress);
        return addressMapper.toAddressDTO(savedAddress);
    }

    @Transactional
    public void setDefaultAddress(String userId, String addressId) {
        log.info("Setting address {} as default for user: {}", addressId, userId);
        
        Address address = findAddressByIdAndUserId(addressId, userId);
        
        if (address.isDefault()) {
            throw new BusinessException("Address is already set as default", "ADDRESS_ALREADY_DEFAULT", HttpStatus.BAD_REQUEST);
        }
        
        // Unset all other default addresses
        addressRepository.unsetDefaultAddressesExcept(userId, addressId);
        
        address.setDefault(true);
        addressRepository.save(address);
        
        log.info("Address set as default successfully: {}", addressId);
        publishAddressEvent("ADDRESS_SET_DEFAULT", userId, address);
    }

    @Transactional
    public void deleteAddress(String userId, String addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        Address address = findAddressByIdAndUserId(addressId, userId);
        
        // Check if this is the only address
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount == 1) {
            throw new BusinessException("Cannot delete the last address", "CANNOT_DELETE_LAST_ADDRESS", HttpStatus.BAD_REQUEST);
        }
        
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);
        
        // If the deleted address was default, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtAsc(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
                log.info("Set address {} as new default after deletion", newDefault.getId());
            }
        }
        
        log.info("Address deleted successfully: {}", addressId);
        publishAddressEvent("ADDRESS_DELETED", userId, address);
    }

    public boolean hasDefaultAddress(String userId) {
        return addressRepository.existsByUserIdAndIsDefaultTrue(userId);
    }

    public long getAddressCount(String userId) {
        return addressRepository.countByUserId(userId);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Address findAddressByIdAndUserId(String addressId, String userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId + " for user: " + userId));
    }

    private void publishAddressEvent(String eventType, String userId, Address address) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("userId", userId);
            event.put("addressId", address.getId());
            event.put("addressData", addressMapper.toAddressDTO(address));
            event.put("timestamp", LocalDateTime.now().toString());
            
            kafkaTemplate.send(KafkaTopics.USER_EVENTS_TOPIC, userId, event);
            log.debug("Published {} event for address: {}", eventType, address.getId());
        } catch (Exception e) {
            log.error("Failed to publish {} event for address: {}", eventType, address.getId(), e);
        }
    }
}