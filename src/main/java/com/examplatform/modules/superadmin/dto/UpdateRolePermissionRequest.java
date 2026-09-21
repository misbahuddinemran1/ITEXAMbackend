package com.examplatform.modules.superadmin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRolePermissionRequest {
    private List<String> hiddenMenuIds;
}
