package com.examplatform.modules.user.service;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.user.dto.ChangePasswordRequest;
import com.examplatform.modules.user.dto.UpdateProfileRequest;
import com.examplatform.modules.user.dto.UserProfileResponse;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserDeviceService userDeviceService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Profile দেখা ─────────────────────────────────────────
    public ApiResponse<UserProfileResponse> getProfile(String userId) {
        User user = findUser(userId);
        return ApiResponse.success("Profile পাওয়া গেছে", buildProfileResponse(user));
    }

    // ─── Profile update ───────────────────────────────────────
    @Transactional
    public ApiResponse<UserProfileResponse> updateProfile(
            String userId, UpdateProfileRequest request) {

        User user = findUser(userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getFullNameBn() != null) {
            user.setFullNameBn(request.getFullNameBn());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getDistrict() != null) {
            user.setDistrict(request.getDistrict());
        }
        if (request.getTargetExam() != null) {
            try {
                user.setTargetExam(
                        User.TargetExam.valueOf(request.getTargetExam().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException(
                        "Target Exam ভুল — BCS_ICT, NTRCA_ICT, BANK_IT, GOVT_IT, OTHER দিতে হবে");
            }
        }
        if (request.getGender() != null) {
            try {
                user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Gender ভুল — MALE, FEMALE, OTHER দিতে হবে");
            }
        }
        if (request.getEducationLevel() != null) {
            try {
                user.setEducationLevel(
                    User.EducationLevel.valueOf(request.getEducationLevel().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException(
                    "Education Level ভুল — HONOURS, ENGINEERING, DEGREE, MASTERS, DIPLOMA, OTHER দিতে হবে");
            }
        }

        userRepository.save(user);
        return ApiResponse.success("Profile update সফল হয়েছে", buildProfileResponse(user));
    }

    // ─── Password change ──────────────────────────────────────
    @Transactional
    public ApiResponse<Void> changePassword(String userId, ChangePasswordRequest request) {

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new ValidationException("নতুন Password কমপক্ষে ৬ অক্ষরের হতে হবে");
        }

        User user = findUser(userId);

        // Google/Facebook user এর password নেই
        if (user.getAuthProvider() != User.AuthProvider.LOCAL) {
            throw new ValidationException(
                user.getAuthProvider().name() + " account এ password change করা যাবে না");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("বর্তমান Password ভুল হয়েছে");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Password বদলালে সব device এর fingerprint login বাতিল
        userDeviceService.revokeAll(userId);

        return ApiResponse.success("Password সফলভাবে পরিবর্তন হয়েছে", null);
    }

    // ─── Helper ───────────────────────────────────────────────
    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User পাওয়া যায়নি"));
    }

    private UserProfileResponse buildProfileResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .fullNameBn(user.getFullNameBn())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .dateOfBirth(user.getDateOfBirth())
                .district(user.getDistrict())
                .educationLevel(user.getEducationLevel() != null
                        ? user.getEducationLevel().name() : null)
                .targetExam(user.getTargetExam() != null ? user.getTargetExam().name() : null)
                .authProvider(user.getAuthProvider().name())
                .isEmailVerified(user.isEmailVerified())
                .isPhoneVerified(user.isPhoneVerified())
                .loginCount(user.getLoginCount())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}