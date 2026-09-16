package com.examplatform.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String fullNameBn;
    private String avatarUrl;
    private String gender;          // MALE, FEMALE, OTHER
    private LocalDate dateOfBirth;
    private String district;
    private String educationLevel;  // HONOURS, ENGINEERING, DEGREE, MASTERS, DIPLOMA, OTHER
    private String targetExam;  // BCS_ICT, NTRCA_ICT, BANK_IT, GOVT_IT, OTHER
}