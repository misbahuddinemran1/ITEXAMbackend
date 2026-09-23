package com.examplatform.modules.user.repository;

import com.examplatform.modules.user.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {

    Optional<UserDevice> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    List<UserDevice> findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(String userId);

    Optional<UserDevice> findByIdAndUserIdAndRevokedAtIsNull(String id, String userId);

    @Modifying
    @Query("update UserDevice d set d.revokedAt = :now where d.userId = :userId and d.revokedAt is null")
    int revokeAllByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
}
