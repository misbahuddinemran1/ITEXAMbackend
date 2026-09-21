package com.examplatform.modules.superadmin.repository;

import com.examplatform.modules.superadmin.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
}
