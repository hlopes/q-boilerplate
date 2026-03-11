package com.example.service;

import com.example.config.AppConfig;
import com.example.dto.UserDto;
import com.example.entity.User;
import com.example.exception.ConflictException;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Service layer for User business logic. Handles CRUD, validation, caching, and password hashing.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserService {

  private static final String USER_CACHE = "user-cache";

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final AppConfig appConfig;

  /**
   * Retrieves a user by ID with cache support.
   *
   * @param id the user's primary key
   * @return the user response DTO
   * @throws ResourceNotFoundException if no user with the given id exists
   */
  @CacheResult(cacheName = USER_CACHE)
  public UserDto.UserResponse findById(UUID id) {
    Log.debugf("Fetching user with id=%s", id);
    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

    return userMapper.toResponse(user);
  }

  /**
   * Returns a paginated list of active users.
   *
   * @param page 0-based page number
   * @param size number of items per page
   * @return paginated user response
   */
  public UserDto.UserPageResponse listUsers(int page, int size) {
    int effectiveSize = Math.min(size, appConfig.pagination().maxPageSize());
    List<UserDto.UserResponse> users =
        userRepository.findAllActive(page, effectiveSize).stream()
            .map(userMapper::toResponse)
            .toList();

    long total = userRepository.countActive();
    int totalPages = (int) Math.ceil((double) total / effectiveSize);

    return new UserDto.UserPageResponse(users, page, effectiveSize, total, totalPages);
  }

  /**
   * Creates a new user after validating uniqueness constraints.
   *
   * @param request the create request DTO
   * @return the created user's response DTO
   * @throws ConflictException if the email or username is already taken
   */
  @Transactional
  public UserDto.UserResponse createUser(UserDto.CreateUserRequest request) {
    Log.infof("Creating user with username=%s", request.username());

    if (userRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email '" + request.email() + "' is already registered");
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new ConflictException("Username '" + request.username() + "' is already taken");
    }

    User user = userMapper.toEntity(request);
    user.passwordHash = hashPassword(request.password());
    userRepository.persist(user);

    Log.infof("User created with id=%s", user.id);

    return userMapper.toResponse(user);
  }

  /**
   * Updates an existing user's mutable fields.
   *
   * @param id the user id
   * @param request the update request DTO
   * @return the updated user response DTO
   */
  @Transactional
  @CacheInvalidate(cacheName = USER_CACHE)
  public UserDto.UserResponse updateUser(UUID id, UserDto.UpdateUserRequest request) {
    Log.debugf("Updating user id=%s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

    if (request.email() != null
        && !request.email().equals(user.email)
        && userRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email '" + request.email() + "' is already registered");
    }

    userMapper.updateEntity(request, user);

    return userMapper.toResponse(user);
  }

  /**
   * Deactivates a user account (soft delete).
   *
   * @param id the user id
   */
  @Transactional
  @CacheInvalidate(cacheName = USER_CACHE)
  public void deactivateUser(UUID id) {
    Log.infof("Deactivating user id=%s", id);
    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    user.status = User.UserStatus.INACTIVE;
  }

  /**
   * Permanently deletes a user by ID.
   *
   * @param id the user id
   */
  @Transactional
  @CacheInvalidate(cacheName = USER_CACHE)
  public void deleteUser(UUID id) {
    Log.infof("Deleting user id=%s", id);
    boolean deleted = userRepository.deleteById(id);

    if (!deleted) {
      throw new ResourceNotFoundException("User", id);
    }
  }

  /**
   * Searches users by keyword across username and email fields.
   *
   * @param keyword search term
   * @param page page index
   * @param size page size
   * @return list of matching user response DTOs
   */
  public List<UserDto.UserResponse> searchUsers(String keyword, int page, int size) {
    int effectiveSize = Math.min(size, appConfig.pagination().maxPageSize());

    return userRepository.search(keyword, page, effectiveSize).stream()
        .map(userMapper::toResponse)
        .toList();
  }

  // -------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------

  /**
   * Placeholder for a real password hashing implementation. In production, use bcrypt via
   * quarkus-elytron-security-properties-file or Argon2 via a dedicated library.
   *
   * @param plainText the raw password
   * @return hashed password string
   */
  private String hashPassword(String plainText) {
    // TODO: replace with BCrypt.hashpw(plainText, BCrypt.gensalt())
    return Integer.toHexString(plainText.hashCode());
  }
}
