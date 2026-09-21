package com.examplatform.modules.superadmin.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.superadmin.dto.AdminAccountResponse;
import com.examplatform.modules.superadmin.dto.CreateAdminAccountRequest;
import com.examplatform.modules.superadmin.dto.UpdateAdminAccountRequest;
import com.examplatform.modules.superadmin.service.AdminAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * শুধু SUPER_ADMIN role এর জন্য (SecurityConfig এ /superadmin/** বন্ধ করা আছে)।
 */
@RestController
@RequestMapping("/superadmin/admins")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminAccountResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(adminAccountService.listAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminAccountResponse>> create(
            @Valid @RequestBody CreateAdminAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Admin তৈরি হয়েছে", adminAccountService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> update(
            @PathVariable String id,
            @RequestBody UpdateAdminAccountRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Admin আপডেট হয়েছে",
                adminAccountService.update(id, request, authentication.getName())));
    }
}
