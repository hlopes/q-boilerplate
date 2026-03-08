package com.example.dto;

import com.example.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Objects for User operations.
 * Uses Java 21 records for immutable, concise DTOs.
 */
public final class UserDto {

    /** Request DTO for creating a new user */
    public record CreateUserRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50)
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @Size(max = 100)
            String firstName,

            @Size(max = 100)
            String lastName
    ) {}

    /** Request DTO for updating an existing user */
    public record UpdateUserRequest(
            @Size(max = 100)
            String firstName,

            @Size(max = 100)
            String lastName,

            @Email(message = "Must be a valid email address")
            String email
    ) {}

    /** Response DTO — safe to expose to clients (no password) */
    public record UserResponse(
            Long id,
            String username,
            String email,
            String firstName,
            String lastName,
            User.UserRole role,
            User.UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    /** Paginated list response */
    public record UserPageResponse(
            java.util.List<UserResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    /** Login request */
    public record LoginRequest(
            @NotBlank(message = "Username or email is required")
            String usernameOrEmail,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    /** Login / token response */
    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserResponse user
    ) {}

    private UserDto() {}
}
