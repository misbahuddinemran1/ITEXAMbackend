package com.examplatform.modules.superadmin.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.superadmin.dto.MyPermissionResponse;
import com.examplatform.modules.superadmin.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * লগইন করা admin নিজের role এর লুকানো মেনুর তালিকা জানতে ব্যবহার করে।
 */
@RestController
@RequestMapping("/auth/my-permissions")
@RequiredArgsConstructor
public class MyPermissionController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RolePermissionService rolePermissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPermissionResponse>> mine(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.<MyPermissionResponse>error("Unauthorized"));
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(ApiResponse.<MyPermissionResponse>error("Unauthorized"));
        }

        String role = jwtTokenProvider.getRoleFromToken(token);
        return ResponseEntity.ok(ApiResponse.success(
                new MyPermissionResponse(role, rolePermissionService.getHiddenFor(role))));
    }
}
