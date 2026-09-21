package com.examplatform.modules.superadmin.service;

import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.superadmin.entity.RolePermission;
import com.examplatform.modules.superadmin.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private static final Pattern MENU_ID = Pattern.compile("^[a-z0-9-]{1,60}$");

    private final RolePermissionRepository repository;

    /** সব বদলানো-যায়-এমন role এর লুকানো মেনু (SUPER_ADMIN বাদে) */
    @Transactional(readOnly = true)
    public Map<String, List<String>> getAll() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (AdminUser.AdminRole role : AdminUser.AdminRole.values()) {
            if (role == AdminUser.AdminRole.SUPER_ADMIN) continue;
            result.put(role.name(), getHiddenFor(role.name()));
        }
        return result;
    }

    /** নির্দিষ্ট role এর লুকানো মেনুর তালিকা; SUPER_ADMIN বা অজানা role এ ফাঁকা */
    @Transactional(readOnly = true)
    public List<String> getHiddenFor(String role) {
        if (role == null || "SUPER_ADMIN".equals(role)) return List.of();
        return repository.findById(role)
                .map(rp -> split(rp.getHiddenMenuIds()))
                .orElse(List.of());
    }

    @Transactional
    public List<String> update(String roleName, List<String> hiddenMenuIds) {
        AdminUser.AdminRole role;
        try {
            role = AdminUser.AdminRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("অজানা role: " + roleName);
        }
        if (role == AdminUser.AdminRole.SUPER_ADMIN) {
            throw new ValidationException("Super Admin এর permission বদলানো যায় না");
        }

        List<String> clean = (hiddenMenuIds == null ? List.<String>of() : hiddenMenuIds).stream()
                .filter(id -> id != null)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .toList();

        if (clean.size() > 200) {
            throw new ValidationException("অনেক বেশি মেনু আইডি");
        }
        for (String id : clean) {
            if (!MENU_ID.matcher(id).matches()) {
                throw new ValidationException("অবৈধ মেনু আইডি: " + id);
            }
            if ("dashboard".equals(id)) {
                throw new ValidationException("Dashboard লুকানো যাবে না");
            }
        }

        RolePermission rp = repository.findById(role.name())
                .orElseGet(() -> RolePermission.builder().role(role.name()).build());
        rp.setHiddenMenuIds(String.join(",", clean));
        repository.save(rp);
        return clean;
    }

    private List<String> split(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
