package com.examplatform.modules.user.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String fullNameBn;
    private String email;       // optional
    private String phone;       // required
    private String password;
    private String institutionName;  // স্কুল/কলেজ নাম
    private String educationLevel;   // SSC / HSC
    private String session;          // যেমন 2024-2025
    private String district;         // ৮টা বিভাগ/জেলার একটা
    private String referredBy;
    private String targetExam;  // optional: BCS_ICT, NTRCA_ICT, BANK_IT, GOVT_IT, OTHER
}
