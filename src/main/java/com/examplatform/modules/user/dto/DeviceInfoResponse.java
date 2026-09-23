package com.examplatform.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoResponse {
    private String id;
    private String deviceName;
    private String platform;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
