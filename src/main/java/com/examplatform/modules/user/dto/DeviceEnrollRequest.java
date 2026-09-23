package com.examplatform.modules.user.dto;

import lombok.Data;

@Data
public class DeviceEnrollRequest {
    private String deviceName;
    private String platform;
}
