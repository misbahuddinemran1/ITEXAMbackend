package com.examplatform.modules.superadmin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminAccountResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
