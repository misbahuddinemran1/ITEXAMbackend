package com.examplatform.modules.superadmin.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.superadmin.dto.UpdateRolePermissionRequest;
import com.examplatform.modules.superadmin.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * শুধু SUPER_ADMIN (SecurityConfig এ /superadmin/** বন্ধ করা আছে)।
 */
@RestController
@RequestMapping("/superadmin/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(rolePermissionService.getAll()));
    }

    @PutMapping("/{role}")
    public ResponseEntity<ApiResponse<List<String>>> update(
            @PathVariable String role,
            @RequestBody UpdateRolePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Permission সেভ হয়েছে",
                rolePermissionService.update(role, request.getHiddenMenuIds())));
    }
}
