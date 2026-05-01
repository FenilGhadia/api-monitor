package com.apimonitor.modules.user.controller;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.common.response.ApiResponse;
import com.apimonitor.modules.user.dto.ChangePasswordRequest;
import com.apimonitor.modules.user.dto.UserDto;
import com.apimonitor.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management.
 *
 * Routes:
 *  GET    /api/v1/users/me              → own profile (any authenticated user)
 *  PATCH  /api/v1/users/me/password     → change own password
 *  GET    /api/v1/users                 → list all users (ADMIN)
 *  GET    /api/v1/users/{id}            → get user by id (ADMIN)
 *  PATCH  /api/v1/users/{id}/toggle     → enable/disable account (ADMIN)
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Returns the currently authenticated user's profile */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserDto dto = userService.getByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", dto));
    }

    /** Change own password — verifies current password before updating */
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(
                userDetails.getUsername(),
                request.currentPassword(),
                request.newPassword()
        );
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully"));
    }

    /** Paginated list of all users — ADMIN only */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        if (pageable.getPageSize() > 100) {
            throw new ApiException("Page size must not exceed 100", HttpStatus.BAD_REQUEST);
        }
        Page<UserDto> page = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", page));
    }

    /** Get a specific user by ID — ADMIN only */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved", userService.getById(id)));
    }

    /** Enable or disable a user account — ADMIN only */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal com.apimonitor.modules.user.entity.User currentUser) {

        if (!enabled && currentUser.getId().equals(id)) {
            throw new com.apimonitor.common.exception.ApiException(
                    "Cannot disable your own account", HttpStatus.BAD_REQUEST);
        }
        UserDto updated = userService.toggleEnabled(id, enabled);
        String msg = enabled ? "User account enabled" : "User account disabled";
        return ResponseEntity.ok(ApiResponse.success(msg, updated));
    }
}