package com.examplatform.modules.leaderboard.controller;

import com.examplatform.modules.leaderboard.dto.LeaderboardSettingsUpdateRequest;
import com.examplatform.modules.leaderboard.entity.LeaderboardSettings;
import com.examplatform.modules.leaderboard.service.OverallLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Admin app এর Leaderboard স্ক্রিন সরাসরি object পড়ে (ApiResponse মোড়ক ছাড়া), তাই এখানেও তাই।
@RestController
@RequestMapping("/admin/leaderboard")
@RequiredArgsConstructor
public class AdminLeaderboardController {

    private final OverallLeaderboardService leaderboardService;

    @GetMapping("/settings")
    public LeaderboardSettings getSettings() {
        return leaderboardService.getSettings();
    }

    @PutMapping("/settings")
    public LeaderboardSettings updateSettings(
            @RequestBody LeaderboardSettingsUpdateRequest req,
            Authentication auth) {
        String adminId = auth != null ? auth.getName() : null;
        return leaderboardService.updateSettings(req, adminId);
    }
}
