package com.apimonitor.modules.user.service;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.modules.user.dto.UserDto;
import com.apimonitor.modules.user.entity.User;
import com.apimonitor.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return UserDto.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserDto getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::from)
                .orElseThrow(() -> new ApiException(
                        "User not found: " + email, HttpStatus.NOT_FOUND));
    }


    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::from);
    }



    @Transactional
    public UserDto toggleEnabled(Long id, boolean enabled) {
        User user = findOrThrow(id);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        log.info("User [{}] {} by admin", saved.getEmail(), enabled ? "enabled" : "disabled");
        return UserDto.from(saved);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        if (newPassword.length() < 8) {
            throw new ApiException("New password must be at least 8 characters", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user [{}]", email);
    }



    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "User not found: " + id, HttpStatus.NOT_FOUND));
    }
}