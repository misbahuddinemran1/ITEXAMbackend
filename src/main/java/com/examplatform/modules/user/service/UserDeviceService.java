package com.examplatform.modules.user.service;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.common.exception.ValidationException;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.user.dto.AuthResponse;
import com.examplatform.modules.user.dto.DeviceEnrollRequest;
import com.examplatform.modules.user.dto.DeviceEnrollResponse;
import com.examplatform.modules.user.dto.DeviceInfoResponse;
import com.examplatform.modules.user.dto.DeviceLoginRequest;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.entity.UserDevice;
import com.examplatform.modules.user.repository.UserDeviceRepository;
import com.examplatform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private static final int MAX_DEVICES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserDeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // ─── Enroll: fingerprint চালু করলে এই device এর জন্য token তৈরি ───
    @Transactional
    public ApiResponse<DeviceEnrollResponse> enroll(String userId, DeviceEnrollRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User পাওয়া যায়নি"));
        if (!user.isActive()) {
            throw new ValidationException("এই account টি বন্ধ করা হয়েছে");
        }

        // সর্বোচ্চ ৫টা device — বেশি হলে সবচেয়ে পুরোনোটা বাতিল
        List<UserDevice> active =
                deviceRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId);
        if (active.size() >= MAX_DEVICES) {
            UserDevice oldest = active.get(active.size() - 1);
            oldest.setRevokedAt(LocalDateTime.now());
            deviceRepository.save(oldest);
        }

        String rawToken = generateRawToken();

        UserDevice device = UserDevice.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .deviceName(cut(req != null ? req.getDeviceName() : null, 100))
                .platform(cut(req != null ? req.getPlatform() : null, 20))
                .lastUsedAt(LocalDateTime.now())
                .build();
        deviceRepository.save(device);

        return ApiResponse.success("Fingerprint login চালু হয়েছে",
                DeviceEnrollResponse.builder()
                        .deviceId(device.getId())
                        .deviceToken(rawToken)
                        .build());
    }

    // ─── Biometric Login: device token দিয়ে JWT ─────────────────────
    @Transactional
    public ApiResponse<AuthResponse> login(DeviceLoginRequest req) {
        if (req == null || req.getDeviceToken() == null || req.getDeviceToken().isBlank()) {
            throw new ValidationException("Device token দিতে হবে");
        }

        String revokedMsg = "এই device এর fingerprint login বন্ধ আছে, password দিয়ে login করুন";

        UserDevice device = deviceRepository
                .findByTokenHashAndRevokedAtIsNull(hash(req.getDeviceToken()))
                .orElseThrow(() -> new ValidationException(revokedMsg));

        User user = userRepository.findById(device.getUserId())
                .orElseThrow(() -> new ValidationException(revokedMsg));

        if (!user.isActive()) {
            throw new ValidationException("এই account টি বন্ধ করা হয়েছে");
        }

        LocalDateTime now = LocalDateTime.now();
        device.setLastUsedAt(now);
        deviceRepository.save(device);

        user.setLastLoginAt(now);
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), "USER");

        return ApiResponse.success("Login সফল হয়েছে",
                AuthResponse.builder()
                        .token(token)
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .authProvider(user.getAuthProvider().name())
                        .isEmailVerified(user.isEmailVerified())
                        .isPhoneVerified(user.isPhoneVerified())
                        .build());
    }

    // ─── Device list / revoke ────────────────────────────────────
    @Transactional(readOnly = true)
    public ApiResponse<List<DeviceInfoResponse>> listDevices(String userId) {
        List<DeviceInfoResponse> list = deviceRepository
                .findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(d -> DeviceInfoResponse.builder()
                        .id(d.getId())
                        .deviceName(d.getDeviceName())
                        .platform(d.getPlatform())
                        .createdAt(d.getCreatedAt())
                        .lastUsedAt(d.getLastUsedAt())
                        .build())
                .toList();
        return ApiResponse.success(list);
    }

    @Transactional
    public ApiResponse<Void> revoke(String userId, String deviceId) {
        UserDevice device = deviceRepository
                .findByIdAndUserIdAndRevokedAtIsNull(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Device পাওয়া যায়নি"));
        device.setRevokedAt(LocalDateTime.now());
        deviceRepository.save(device);
        return ApiResponse.success("Device সরানো হয়েছে", null);
    }

    @Transactional
    public int revokeAll(String userId) {
        return deviceRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    // ─── Helpers ─────────────────────────────────────────────────
    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String cut(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}
