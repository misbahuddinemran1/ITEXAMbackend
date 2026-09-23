package com.examplatform.modules.user.service;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.common.exception.ValidationException;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.subscription.service.SubscriptionService;
import com.examplatform.modules.user.dto.AuthResponse;
import com.examplatform.modules.user.dto.LoginRequest;
import com.examplatform.modules.user.dto.RegisterRequest;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SubscriptionService subscriptionService;

    // ─── Registration ─────────────────────────────────────────
    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest request) {

        // Validation
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ValidationException("নাম দিতে হবে");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new ValidationException("Phone নম্বর দিতে হবে");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ValidationException("Password কমপক্ষে ৬ অক্ষরের হতে হবে");
        }
        if (request.getInstitutionName() == null || request.getInstitutionName().isBlank()) {
            throw new ValidationException("School/College নাম দিতে হবে");
        }
        if (request.getEducationLevel() == null || request.getEducationLevel().isBlank()) {
            throw new ValidationException("Class (SSC/HSC) সিলেক্ট করতে হবে");
        }
        if (request.getDistrict() == null || request.getDistrict().isBlank()) {
            throw new ValidationException("জেলা সিলেক্ট করতে হবে");
        }

        // Duplicate check
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("এই Email দিয়ে আগেই account আছে");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("এই Phone নম্বর দিয়ে আগেই account আছে");
        }

        // Education Level validation
        User.EducationLevel educationLevel;
        try {
            educationLevel = User.EducationLevel.valueOf(request.getEducationLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Class ভুল — SSC অথবা HSC দিতে হবে");
        }

        // Target Exam validation (optional)
        User.TargetExam targetExam = null;
        if (request.getTargetExam() != null && !request.getTargetExam().isBlank()) {
            try {
                targetExam = User.TargetExam.valueOf(request.getTargetExam().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Target Exam ভুল — সঠিক পরীক্ষা সিলেক্ট করুন");
            }
        }

        // User build
        User user = User.builder()
                .fullName(request.getFullName())
                .fullNameBn(request.getFullNameBn())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .authProvider(User.AuthProvider.LOCAL)
                .institutionName(request.getInstitutionName())
                .educationLevel(educationLevel)
                .targetExam(targetExam)
                .session(request.getSession())
                .district(request.getDistrict())
                .referredBy(request.getReferredBy())
                .build();

        userRepository.save(user);

        // ✅ Trial Subscription দেওয়া হচ্ছে
        subscriptionService.createTrialSubscription(user.getId());

        // Token
        String token = jwtTokenProvider.generateToken(user.getId(), "USER");

        return ApiResponse.success("Registration সফল হয়েছে", buildAuthResponse(token, user));
    }

    // ─── Login ────────────────────────────────────────────────
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {

        if (request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new ValidationException("Phone নম্বর দিতে হবে");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ValidationException("Password দিতে হবে");
        }

        // Email অথবা Phone দিয়ে খোঁজা
        User user = findByEmailOrPhone(request.getIdentifier());

        // Active check
        if (!user.isActive()) {
            throw new ValidationException("এই account টি বন্ধ করা হয়েছে");
        }

        // Password check
        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidationException("Phone অথবা Password ভুল হয়েছে");
        }

        // Last login update
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), "USER");

        return ApiResponse.success("Login সফল হয়েছে", buildAuthResponse(token, user));
    }

    // ─── Helper Methods ───────────────────────────────────────
    private User findByEmailOrPhone(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Email বা Password ভুল হয়েছে"));
        } else {
            return userRepository.findByPhone(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Phone বা Password ভুল হয়েছে"));
        }
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .authProvider(user.getAuthProvider().name())
                .isEmailVerified(user.isEmailVerified())
                .isPhoneVerified(user.isPhoneVerified())
                .build();
    }
}
