package com.apimonitor.modules.user.dto;

import com.apimonitor.modules.user.entity.User;

import java.time.LocalDateTime;


public record UserDto(
        Long id,
        String username,
        String email,
        String role,
        boolean enabled,
        LocalDateTime createdAt
) {

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