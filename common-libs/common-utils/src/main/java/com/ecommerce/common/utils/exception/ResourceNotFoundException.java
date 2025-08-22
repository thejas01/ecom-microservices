package com.ecommerce.common.utils.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource, String field, String value) {
        super(
            "RESOURCE_NOT_FOUND",
            String.format("%s not found with %s: '%s'", resource, field, value),
            HttpStatus.NOT_FOUND,
            new Object[]{resource, field, value}
        );
    }
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}