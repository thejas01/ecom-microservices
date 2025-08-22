package com.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Constructors
    public PasswordResetRequest() {}

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    // Getters
    public String getEmail() { return email; }

    // Setters
    public void setEmail(String email) { this.email = email; }
}