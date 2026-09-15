package com.examplatform.modules.written.settings.repository;

import com.examplatform.modules.written.settings.entity.WrittenSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrittenSettingsRepository extends JpaRepository<WrittenSettings, String> {
}
