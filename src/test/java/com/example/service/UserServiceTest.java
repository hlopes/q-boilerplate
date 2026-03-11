package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.dto.UserDto;
import com.example.entity.User;
import com.example.exception.ConflictException;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link UserService}. */
@QuarkusTest
class UserServiceTest {

  @Inject UserService userService;

  @InjectMock UserRepository userRepository;

  @InjectMock UserMapper userMapper;

  private User sampleUser;
  private UserDto.UserResponse sampleResponse;
  private final UUID sampleId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    sampleUser = new User();
    sampleUser.id = sampleId;
    sampleUser.username = "johndoe";
    sampleUser.email = "john@example.com";
    sampleUser.firstName = "John";
    sampleUser.lastName = "Doe";
    sampleUser.role = User.UserRole.USER;
    sampleUser.status = User.UserStatus.ACTIVE;
    sampleUser.createdAt = LocalDateTime.now();
    sampleUser.updatedAt = LocalDateTime.now();

    sampleResponse =
        new UserDto.UserResponse(
            sampleId,
            "johndoe",
            "john@example.com",
            "John",
            "Doe",
            User.UserRole.USER,
            User.UserStatus.ACTIVE,
            sampleUser.createdAt,
            sampleUser.updatedAt);
  }

  // -------------------------------------------------------
  // findById
  // -------------------------------------------------------

  @Test
  @DisplayName("findById - should return user when found")
  void findById_whenUserExists_returnsResponse() {
    when(userRepository.findByIdOptional(sampleId)).thenReturn(Optional.of(sampleUser));
    when(userMapper.toResponse(sampleUser)).thenReturn(sampleResponse);

    UserDto.UserResponse result = userService.findById(sampleId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(sampleId);
    assertThat(result.username()).isEqualTo("johndoe");
  }

  @Test
  @DisplayName("findById - should throw ResourceNotFoundException when user not found")
  void findById_whenUserNotFound_throwsNotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(userRepository.findByIdOptional(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(nonExistentId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // -------------------------------------------------------
  // createUser
  // -------------------------------------------------------

  @Test
  @DisplayName("createUser - should create successfully when username and email are unique")
  void createUser_whenValid_returnsCreated() {
    UserDto.CreateUserRequest request =
        new UserDto.CreateUserRequest("newuser", "new@example.com", "Password123!", "New", "User");

    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userMapper.toEntity(request)).thenReturn(sampleUser);
    when(userMapper.toResponse(any(User.class))).thenReturn(sampleResponse);
    doNothing().when(userRepository).persist(any(User.class));

    UserDto.UserResponse result = userService.createUser(request);

    assertThat(result).isNotNull();
    verify(userRepository).persist(any(User.class));
  }

  @Test
  @DisplayName("createUser - should throw ConflictException when email is taken")
  void createUser_whenEmailTaken_throwsConflict() {
    UserDto.CreateUserRequest request =
        new UserDto.CreateUserRequest(
            "newuser", "taken@example.com", "Password123!", "New", "User");

    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.createUser(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("taken@example.com");
  }

  @Test
  @DisplayName("createUser - should throw ConflictException when username is taken")
  void createUser_whenUsernameTaken_throwsConflict() {
    UserDto.CreateUserRequest request =
        new UserDto.CreateUserRequest("johndoe", "unique@example.com", "Password123!", "John", "D");

    when(userRepository.existsByEmail("unique@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("johndoe")).thenReturn(true);

    assertThatThrownBy(() -> userService.createUser(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("johndoe");
  }

  // -------------------------------------------------------
  // deleteUser
  // -------------------------------------------------------

  @Test
  @DisplayName("deleteUser - should throw ResourceNotFoundException when user does not exist")
  void deleteUser_whenNotFound_throwsException() {
    UUID nonExistentId = UUID.randomUUID();
    when(userRepository.deleteById(nonExistentId)).thenReturn(false);

    assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
