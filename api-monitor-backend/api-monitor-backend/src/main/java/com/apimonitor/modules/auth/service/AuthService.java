package com.apimonitor.modules.auth.service;

import com.apimonitor.common.exception.ApiException;
import com.apimonitor.common.security.JwtService;
import com.apimonitor.modules.auth.dto.AuthResponse;
import com.apimonitor.modules.auth.dto.LoginRequest;
import com.apimonitor.modules.auth.dto.RegisterRequest;
import com.apimonitor.modules.user.entity.Role;
import com.apimonitor.modules.user.entity.User;
import com.apimonitor.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;




    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(
                    "Email already registered: " + request.email(), HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(
                    "Username already taken: " + request.username(), HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Admin account created: {} ({})", saved.getUsername(), saved.getEmail());

        return buildAuthResponse(saved);
    }



    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }




    public AuthResponse refreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ApiException("Refresh token is required", HttpStatus.BAD_REQUEST);
        }

        String email = jwtService.extractUsername(rawRefreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!jwtService.isTokenValid(rawRefreshToken, user)) {
            throw new ApiException("Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED);
        }


        return new AuthResponse(
                jwtService.generateAccessToken(user),
                rawRefreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }



    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}