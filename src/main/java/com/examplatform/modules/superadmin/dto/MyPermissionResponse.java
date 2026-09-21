package com.examplatform.modules.superadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyPermissionResponse {
    private String role;
    private List<String> hiddenMenuIds;
}
