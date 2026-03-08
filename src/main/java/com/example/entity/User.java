package com.example.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User entity representing application users.
 * Extends PanacheEntity for built-in id and active record helpers.
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class User extends PanacheEntity {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    public String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @NotBlank(message = "Password hash is required")
    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    public String firstName;

    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    public String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    public UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** User roles */
    public enum UserRole {
        ADMIN, USER, MODERATOR
    }

    /** User account status */
    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
