package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetConfirmRequest {

    @NotBlank(message = "Reset token is required")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String newPassword;

    // Constructors
    public PasswordResetConfirmRequest() {}

    public PasswordResetConfirmRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    // Getters
    public String getResetToken() { return resetToken; }
    public String getNewPassword() { return newPassword; }

    // Setters
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}