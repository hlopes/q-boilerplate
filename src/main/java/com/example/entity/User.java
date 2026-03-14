package com.example.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** User entity representing application users. Extends PanacheEntityBase for custom id support. */
@Entity
@Table(
        name = "users",
        indexes = {
            @Index(name = "idx_users_email", columnList = "email", unique = true),
            @Index(name = "idx_users_status", columnList = "status"),
            @Index(name = "idx_users_tenant", columnList = "tenant_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Size(max = 100) @Column(name = "first_name", length = 100)
    public String firstName;

    @Size(max = 100) @Column(name = "last_name", length = 100)
    public String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    public UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public UserStatus status = UserStatus.ACTIVE;

    @Column(name = "picture")
    public String picture;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Tenant tenant;

    /** User roles */
    public enum UserRole {
        ADMIN,
        USER,
        MODERATOR
    }

    /** User account status */
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    @Transactional
    public static User upsert(String email, String picture, Tenant tenant) {
        User user = find("email", email.toLowerCase()).firstResult();

        if (user == null) {
            user = new User();
            user.email = email.toLowerCase();
        }

        user.picture = picture;
        user.tenant = tenant;
        user.persist();

        return user;
    }
}
