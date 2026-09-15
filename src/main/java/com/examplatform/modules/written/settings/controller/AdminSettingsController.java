package com.examplatform.modules.written.settings.controller;

import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.written.settings.request.UpdateSettingsRequest;
import com.examplatform.modules.written.settings.response.SettingsResponse;
import com.examplatform.modules.written.settings.service.WrittenSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final WrittenSettingsService settingsService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping
    public SettingsResponse getSettings() {
        return settingsService.getSettings();
    }

    @PutMapping
    public SettingsResponse updateSettings(@Valid @RequestBody UpdateSettingsRequest request,
                                            Authentication auth) {
        String adminId = resolveAdminId(auth);
        return settingsService.updateSettings(request, adminId);
    }

    private String resolveAdminId(Authentication auth) {
        return adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()))
                .getId();
    }
}
