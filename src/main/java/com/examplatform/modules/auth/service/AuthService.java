package com.examplatform.modules.auth.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.auth.dto.LoginRequest;
import com.examplatform.modules.auth.dto.LoginResponse;
import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import com.examplatform.infrastructure.security.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public LoginResponse login(LoginRequest request) {
        String u = request.getUsername();
        if (u == null || u.isBlank()) {
            return doLogin(request);
        }
        String key = "admin:" + u.trim().toLowerCase();
        loginAttemptService.checkAllowed(key);
        try {
            LoginResponse res = doLogin(request);
            loginAttemptService.recordSuccess(key);
            return res;
        } catch (ResourceNotFoundException e) {
            loginAttemptService.recordFailure(key);
            throw e;
        }
    }

    private LoginResponse doLogin(LoginRequest request) {

        AdminUser admin = adminUserRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Invalid username or password"
                    )
                );

        if (!admin.isActive()) {
            throw new ResourceNotFoundException(
                "Account is disabled"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                admin.getPasswordHash())) {
            throw new ResourceNotFoundException(
                "Invalid username or password"
            );
        }

        // Last login update
        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);

        String token = jwtTokenProvider.generateToken(
            admin.getUsername(),
            admin.getRole().name()
        );

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .username(admin.getUsername())
                .fullName(admin.getFullName())
                .role(admin.getRole().name())
                .build();
    }
}