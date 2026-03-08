package com.example.mapper;

import com.example.dto.UserDto;
import com.example.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between {@link User} entities and DTOs.
 * CDI component model is enabled via the compiler arg in pom.xml.
 */
@Mapper(
    componentModel = "cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * Maps a User entity to a UserResponse DTO.
     *
     * @param user the source entity
     * @return the response DTO
     */
    UserDto.UserResponse toResponse(User user);

    /**
     * Maps a CreateUserRequest to a User entity.
     * Password mapping is intentionally excluded here — hashing happens in the service.
     *
     * @param request the create request
     * @return the new User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto.CreateUserRequest request);

    /**
     * Updates an existing User entity from an UpdateUserRequest.
     * Null fields in the request are ignored (partial update).
     *
     * @param request the update request
     * @param user    the entity to update (modified in-place)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserDto.UpdateUserRequest request, @MappingTarget User user);
}
