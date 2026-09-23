package com.examplatform.modules.user.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.common.exception.ValidationException;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.user.dto.AuthResponse;
import com.examplatform.modules.user.dto.DeviceEnrollRequest;
import com.examplatform.modules.user.dto.DeviceEnrollResponse;
import com.examplatform.modules.user.dto.DeviceInfoResponse;
import com.examplatform.modules.user.dto.DeviceLoginRequest;
import com.examplatform.modules.user.service.UserDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/auth/biometric")
@RequiredArgsConstructor
@Tag(name = "User Biometric Login", description = "Fingerprint login — device token")
public class UserDeviceController {

    private final UserDeviceService deviceService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Device token দিয়ে Login (fingerprint যাচাই ফোনেই হয়)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody DeviceLoginRequest body) {
        return ResponseEntity.ok(deviceService.login(body));
    }

    @PostMapping("/enroll")
    @Operation(summary = "এই device এ Fingerprint login চালু করা")
    public ResponseEntity<ApiResponse<DeviceEnrollResponse>> enroll(
            HttpServletRequest request,
            @RequestBody(required = false) DeviceEnrollRequest body) {
        return ResponseEntity.ok(deviceService.enroll(extractUserId(request), body));
    }

    @GetMapping("/devices")
    @Operation(summary = "আমার registered device গুলো")
    public ResponseEntity<ApiResponse<List<DeviceInfoResponse>>> devices(
            HttpServletRequest request) {
        return ResponseEntity.ok(deviceService.listDevices(extractUserId(request)));
    }

    @DeleteMapping("/devices/{id}")
    @Operation(summary = "একটা device সরানো")
    public ResponseEntity<ApiResponse<Void>> revoke(
            HttpServletRequest request,
            @PathVariable String id) {
        return ResponseEntity.ok(deviceService.revoke(extractUserId(request), id));
    }

    @DeleteMapping("/devices")
    @Operation(summary = "সব device থেকে Fingerprint login বন্ধ")
    public ResponseEntity<ApiResponse<Void>> revokeAll(HttpServletRequest request) {
        deviceService.revokeAll(extractUserId(request));
        return ResponseEntity.ok(ApiResponse.success("সব device সরানো হয়েছে", null));
    }

    private String extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ValidationException("Authorization token দিতে হবে");
        }
        String token = header.substring(7);
        if (!jwtTokenProvider.validateToken(token)
                || !"USER".equals(jwtTokenProvider.getRoleFromToken(token))) {
            throw new ValidationException("Token সঠিক নয়, আবার login করুন");
        }
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}
