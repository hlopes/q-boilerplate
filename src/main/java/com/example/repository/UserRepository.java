package com.example.repository;

import com.example.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} data access operations.
 * Uses Panache repository pattern for clean data access layer.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    /**
     * Finds all active users with pagination.
     *
     * @param page the page index (0-based)
     * @param size the page size
     * @return list of active users
     */
    public List<User> findAllActive(int page, int size) {
        return find("status", Sort.by("createdAt").descending(), User.UserStatus.ACTIVE)
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Finds users by role with pagination.
     *
     * @param role the role to filter by
     * @param page the page index
     * @param size the page size
     * @return list of users with the given role
     */
    public List<User> findByRole(User.UserRole role, int page, int size) {
        return find("role", Sort.by("username"), role)
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Searches users by username or email containing the given keyword.
     *
     * @param keyword  the search keyword
     * @param page     page index
     * @param size     page size
     * @return matching users
     */
    public List<User> search(String keyword, int page, int size) {
        String likePattern = "%" + keyword.toLowerCase() + "%";
        return find("lower(username) like ?1 or lower(email) like ?1",
                Sort.by("username"), likePattern)
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Counts all active users.
     *
     * @return count of active users
     */
    public long countActive() {
        return count("status", User.UserStatus.ACTIVE);
    }

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if the email is already taken
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to check
     * @return true if the username is already taken
     */
    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }
}
