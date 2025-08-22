package com.ecommerce.auth.security;

import com.ecommerce.auth.entity.UserCredential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final UserCredential userCredential;

    public CustomUserDetails(UserCredential userCredential) {
        this.userCredential = userCredential;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + userCredential.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return userCredential.getPassword();
    }

    @Override
    public String getUsername() {
        return userCredential.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userCredential.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userCredential.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userCredential.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return userCredential.isEnabled();
    }

    public UserCredential getUserCredential() {
        return userCredential;
    }
}