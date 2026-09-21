package com.examplatform.modules.admin.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.admin.dto.AdminUserActivityResponse;
import com.examplatform.modules.admin.dto.AdminUserCountsResponse;
import com.examplatform.modules.admin.dto.AdminUserResponse;
import com.examplatform.modules.admin.dto.GrantSubscriptionRequest;
import com.examplatform.modules.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched",
                        adminUserService.getAllUsers(keyword, page, size))
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                ApiResponse.success("User fetched",
                        adminUserService.getUserById(userId))
        );
    }

    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleStatus(
            @PathVariable String userId) {
        adminUserService.toggleUserStatus(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Status updated", "OK")
        );
    }

    // Users স্ক্রিনের উপরের কার্ডের জন্য মোট সংখ্যা (শুধু বর্তমান পাতার নয়)
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminUserCountsResponse>> getUserCounts() {
        return ResponseEntity.ok(
                ApiResponse.success("User counts fetched",
                        adminUserService.getUserCounts())
        );
    }

    // একজন ছাত্রের পরীক্ষার হিসাব, login সংখ্যা ও subscription ইতিহাস
    @GetMapping("/{userId}/activity")
    public ResponseEntity<ApiResponse<AdminUserActivityResponse>> getUserActivity(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                ApiResponse.success("User activity fetched",
                        adminUserService.getUserActivity(userId))
        );
    }

    @PostMapping("/{userId}/revoke-subscription")
    public ResponseEntity<ApiResponse<String>> revokeSubscription(
            @PathVariable String userId) {
        adminUserService.revokeSubscription(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Subscription revoked", "OK")
        );
    }

    @PostMapping("/{userId}/grant-subscription")
    public ResponseEntity<ApiResponse<String>> grantSubscription(
            @PathVariable String userId,
            @RequestBody GrantSubscriptionRequest request) {
        adminUserService.grantSubscription(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Subscription granted", "OK")
        );
    }
}
