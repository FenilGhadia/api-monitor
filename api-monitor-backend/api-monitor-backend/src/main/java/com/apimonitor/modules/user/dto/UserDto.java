package com.apimonitor.modules.user.dto;

import com.apimonitor.modules.user.entity.User;

import java.time.LocalDateTime;

/**
 * Immutable user DTO using Java 21 record.
 *
 * Never expose the User entity directly — this hides
 * the password field and internal JPA state.
 */
public record UserDto(
        Long id,
        String username,
        String email,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {
    /** Convenience factory — maps from entity */
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}