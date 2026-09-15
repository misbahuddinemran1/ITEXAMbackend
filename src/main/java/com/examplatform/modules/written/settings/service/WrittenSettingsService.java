package com.examplatform.modules.written.settings.service;

import com.examplatform.modules.written.settings.entity.WrittenSettings;
import com.examplatform.modules.written.settings.mapper.WrittenSettingsMapper;
import com.examplatform.modules.written.settings.repository.WrittenSettingsRepository;
import com.examplatform.modules.written.settings.request.UpdateSettingsRequest;
import com.examplatform.modules.written.settings.response.SettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WrittenSettingsService {

    private static final String SETTINGS_ID = "default";

    private final WrittenSettingsRepository settingsRepository;
    private final WrittenSettingsMapper settingsMapper;

    @Transactional
    public SettingsResponse getSettings() {
        WrittenSettings settings = getOrCreateDefault();
        return settingsMapper.toResponse(settings);
    }

    @Transactional
    public SettingsResponse updateSettings(UpdateSettingsRequest request, String adminId) {
        WrittenSettings settings = getOrCreateDefault();
        settingsMapper.applyUpdate(settings, request, adminId);
        settingsRepository.save(settings);
        return settingsMapper.toResponse(settings);
    }

    private WrittenSettings getOrCreateDefault() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> settingsRepository.save(
                        WrittenSettings.builder().id(SETTINGS_ID).build()
                ));
    }
}
