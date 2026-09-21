package com.examplatform.modules.leaderboard.repository;

import com.examplatform.modules.leaderboard.entity.LeaderboardSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardSettingsRepository extends JpaRepository<LeaderboardSettings, String> {
    // id সবসময় "default" — service এ findById("default") ব্যবহার হয়
}
