package com.examplatform.modules.superadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * একটা role এর জন্য কোন কোন মেনু লুকানো আছে (কমা দিয়ে আলাদা করা id)।
 * কোনো row না থাকলে ওই role সব মেনু দেখবে — তাই নতুন মেনু যোগ হলে আপনাআপনি দেখা যাবে।
 */
@Entity
@Table(name = "role_permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @Id
    @Column(name = "role", length = 30)
    private String role;

    @Column(name = "hidden_menu_ids", columnDefinition = "TEXT", nullable = false)
    private String hiddenMenuIds;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
