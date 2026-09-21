package com.examplatform.modules.superadmin.dto;

import com.examplatform.modules.auth.entity.AdminUser;
import lombok.Getter;
import lombok.Setter;

/** যেই field null, সেটা অপরিবর্তিত থাকবে */
@Getter
@Setter
public class UpdateAdminAccountRequest {
    private String fullName;
    private String email;
    private AdminUser.AdminRole role;
    private Boolean active;
    private String newPassword;
}
