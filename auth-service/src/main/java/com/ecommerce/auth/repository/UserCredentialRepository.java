package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, String> {

    Optional<UserCredential> findByUsername(String username);

    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findByUsernameOrEmail(String username, String email);

    Optional<UserCredential> findByResetToken(String resetToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE UserCredential u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") String userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE UserCredential u SET u.resetToken = :token, u.resetTokenExpiresAt = :expiresAt WHERE u.email = :email")
    void setResetToken(@Param("email") String email, @Param("token") String token, @Param("expiresAt") LocalDateTime expiresAt);

    @Modifying
    @Query("UPDATE UserCredential u SET u.password = :password, u.resetToken = null, u.resetTokenExpiresAt = null WHERE u.resetToken = :token")
    void resetPassword(@Param("token") String token, @Param("password") String password);
}