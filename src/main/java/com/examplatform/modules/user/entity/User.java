package com.examplatform.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "full_name_bn", length = 100)
    private String fullNameBn;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone", unique = true, length = 15)
    private String phone;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "institution_name", length = 150)
private String institutionName;

@Column(name = "session", length = 20)
private String session;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevel educationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_exam")
    private TargetExam targetExam;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private boolean isPhoneVerified = false;

    @Column(name = "email_verify_token", length = 100)
    private String emailVerifyToken;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private int loginCount = 0;

    @Column(name = "referred_by", length = 36)
    private String referredBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

   public enum EducationLevel {
        HONOURS,
        ENGINEERING,
        DEGREE,
        MASTERS,
        DIPLOMA,
        OTHER
    }

    public enum TargetExam {
        BCS_ICT,
        NTRCA_ICT,
        BANK_IT,
        GOVT_IT,
        OTHER
    }
}
