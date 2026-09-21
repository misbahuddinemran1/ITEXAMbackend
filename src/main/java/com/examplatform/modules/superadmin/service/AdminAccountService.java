package com.examplatform.modules.superadmin.service;

import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.superadmin.dto.AdminAccountResponse;
import com.examplatform.modules.superadmin.dto.CreateAdminAccountRequest;
import com.examplatform.modules.superadmin.dto.UpdateAdminAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminAccountResponse> listAll() {
        return adminUserRepository.findAll().stream()
                .sorted(Comparator.comparing(AdminUser::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminAccountResponse create(CreateAdminAccountRequest req) {
        String username = req.getUsername().trim();
        String email = req.getEmail().trim();

        if (adminUserRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("এই Username আগে থেকেই আছে");
        }
        if (adminUserRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("এই Email আগে থেকেই আছে");
        }

        AdminUser admin = AdminUser.builder()
                .username(username)
                .email(email)
                .fullName(req.getFullName().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .isActive(true)
                .build();

        return toResponse(adminUserRepository.save(admin));
    }

    /**
     * @param callerUsername যে Super Admin এই পরিবর্তন করছেন (নিজেকে ভুলে বন্ধ করা ঠেকাতে)
     */
    @Transactional
    public AdminAccountResponse update(String id, UpdateAdminAccountRequest req, String callerUsername) {
        AdminUser admin = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin পাওয়া যায়নি"));

        boolean isSelf = admin.getUsername().equals(callerUsername);
        boolean roleChanging = req.getRole() != null && req.getRole() != admin.getRole();
        boolean deactivating = req.getActive() != null && !req.getActive() && admin.isActive();

        if (isSelf && (roleChanging || deactivating)) {
            throw new ValidationException("নিজের Role বদলানো বা নিজের অ্যাকাউন্ট নিষ্ক্রিয় করা যাবে না");
        }

        // সর্বশেষ সক্রিয় Super Admin কে কখনো নামানো/বন্ধ করা যাবে না
        boolean losingSuperPower = admin.getRole() == AdminUser.AdminRole.SUPER_ADMIN && admin.isActive()
                && ((roleChanging && req.getRole() != AdminUser.AdminRole.SUPER_ADMIN) || deactivating);
        if (losingSuperPower && countActiveSuperAdmins() <= 1) {
            throw new ValidationException("সর্বশেষ সক্রিয় Super Admin কে নামানো বা নিষ্ক্রিয় করা যাবে না");
        }

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            admin.setFullName(req.getFullName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && !req.getEmail().trim().equalsIgnoreCase(admin.getEmail())) {
            String email = req.getEmail().trim();
            if (adminUserRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("এই Email আগে থেকেই আছে");
            }
            admin.setEmail(email);
        }
        if (req.getRole() != null) {
            admin.setRole(req.getRole());
        }
        if (req.getActive() != null) {
            admin.setActive(req.getActive());
        }
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (req.getNewPassword().length() < 8) {
                throw new ValidationException("Password কমপক্ষে ৮ অক্ষরের হতে হবে");
            }
            admin.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        }

        return toResponse(adminUserRepository.save(admin));
    }

    private long countActiveSuperAdmins() {
        return adminUserRepository.findAll().stream()
                .filter(a -> a.getRole() == AdminUser.AdminRole.SUPER_ADMIN && a.isActive())
                .count();
    }

    private AdminAccountResponse toResponse(AdminUser a) {
        return AdminAccountResponse.builder()
                .id(a.getId())
                .username(a.getUsername())
                .email(a.getEmail())
                .fullName(a.getFullName())
                .role(a.getRole().name())
                .active(a.isActive())
                .lastLoginAt(a.getLastLoginAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
