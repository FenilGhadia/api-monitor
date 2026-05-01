package com.apimonitor.common.security;

import com.apimonitor.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bridges Spring Security with our User database.
 *
 * Placed in common/security because it is used by both:
 *  - JwtAuthenticationFilter (token validation path)
 *  - ApplicationConfig (DaoAuthenticationProvider for login path)
 *
 * References UserRepository from the user module — the only cross-module
 * dependency intentionally permitted here.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));
    }
}