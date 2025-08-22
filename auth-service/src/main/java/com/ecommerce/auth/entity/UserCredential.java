package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    private LocalDateTime lastLoginAt;

    private String resetToken;

    private LocalDateTime resetTokenExpiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Role {
        CUSTOMER, ADMIN, VENDOR
    }

    // Constructors
    public UserCredential() {}

    public UserCredential(String id, String username, String email, String password, Role role, 
                         boolean enabled, boolean accountNonExpired, boolean accountNonLocked, 
                         boolean credentialsNonExpired, LocalDateTime lastLoginAt, String resetToken, 
                         LocalDateTime resetTokenExpiresAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.lastLoginAt = lastLoginAt;
        this.resetToken = resetToken;
        this.resetTokenExpiresAt = resetTokenExpiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public boolean isAccountNonExpired() { return accountNonExpired; }
    public boolean isAccountNonLocked() { return accountNonLocked; }
    public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public String getResetToken() { return resetToken; }
    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String email;
        private String password;
        private Role role = Role.CUSTOMER;
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private LocalDateTime lastLoginAt;
        private String resetToken;
        private LocalDateTime resetTokenExpiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder accountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; return this; }
        public Builder accountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; return this; }
        public Builder credentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; return this; }
        public Builder lastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
        public Builder resetToken(String resetToken) { this.resetToken = resetToken; return this; }
        public Builder resetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserCredential build() {
            return new UserCredential(id, username, email, password, role, enabled, 
                                     accountNonExpired, accountNonLocked, credentialsNonExpired, 
                                     lastLoginAt, resetToken, resetTokenExpiresAt, createdAt, updatedAt);
        }
    }
}